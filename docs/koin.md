# Koin in this template: a recap

How dependency injection works here, from the idea down to the error messages. It's written to be read
once and then kept open for reference.

---

## 1. The idea in one paragraph

A class shouldn't build what it needs; it should **receive** it. `PostsRepositoryImpl` needs a
`NetworkClient`, so it takes one in its constructor. Something still has to build every object, in the
right order, exactly once. That something is the **container**, and here it's Koin. You tell Koin *how*
to make each type (a **definition**), and it makes them when asked, passing each one the others it
needs.

If you know the Flutter template, it's the same job `DependencyModule` + `get_it` did:

| Flutter template | Here |
| --- | --- |
| `class PostsModule implements DependencyModule` | `@Module class PostsKoinModule` |
| `container.registerLazySingleton<PostsApi>((_) => PostsApiImpl())` | `@Single fun postsApi(): PostsApi = PostsApiImpl()` |
| `locator<NetworkClient>()` inside the factory | a function parameter: `fun repository(client: NetworkClient)` |
| the module list in `dependency_injection_configuration.dart` | `@Module(includes = [...])` in `DependencyInjectionConfiguration.kt` |
| `context.locator<CounterApi>()` | `koin.get<CounterApi>()`, but only in the app's bootstrap |
| a missing registration crashes at runtime | a missing definition **fails the build** |

The last row is why the compiler plugin is here.

---

## 2. The three pieces

### Definitions: `@Single` functions

```kotlin
@Module
class PostsKoinModule {
    @Single
    fun postsApi(): PostsApi = PostsApiImpl()

    @Single
    internal fun repository(client: NetworkClient): PostsRepository = PostsRepositoryImpl(client)

    @Single(binds = [ModuleRouter::class])
    internal fun moduleRouter(repository: PostsRepository): PostsModuleRouter = PostsModuleRouter(repository)
}
```

Read each function as a sentence:

- the **return type** is *what* it provides (`PostsRepository`);
- the **parameters** are *what it needs* (`NetworkClient`). Koin finds and passes them;
- the **body** is *how* to build it, which is an ordinary constructor call.

`@Single` means one instance for the whole app, created the first time someone asks for it (lazily).

### Modules: `@Module` classes

A module groups a capability's definitions. Every `-impl` has exactly one, named `<Name>KoinModule`, and
it's the only public DI entry point of that module. Modules combine with `includes`:

```kotlin
@Module(includes = [LoggerKoinModule::class, NetworkKoinModule::class, CounterKoinModule::class, PostsKoinModule::class])
internal class AppKoinModule { … }
```

### The application: `@KoinApplication` + `startKoin<…>()`

```kotlin
@KoinApplication(modules = [AppKoinModule::class])
internal object ModularKoinApplication

startKoin<ModularKoinApplication> {
    modules(module { single { config } })   // values from outside the graph, see section 5
}
```

`startKoin` runs once, in `ModularApplication.onCreate()`, and gives back the container (`Koin`).

---

## 3. What the compiler plugin actually does

Classic Koin is a runtime DSL: you write `single { PostsRepositoryImpl(get()) }`, and a missing `get()`
target is only discovered when the app runs that line. The **Koin compiler plugin**
(`io.insert-koin.compiler.plugin`, a Kotlin K2 compiler plugin) moves both jobs into the build:

1. **Generation.** For each `@Module` class, it writes the runtime definitions for you. Each `@Single`
   function becomes a `single { … }` that calls your function with `get()` for every parameter. You never
   see or maintain that code.
2. **Validation.** Where `startKoin<ModularKoinApplication>()` is compiled (in `:app`), it walks the whole
   graph: every module reachable through `includes`, and every parameter of every definition. If a
   parameter has no definition anywhere, **compilation fails**:

   ```
   e: [Koin][KOIN-D001] Missing dependency: com.example.modularapp.core.network.NetworkClient
   ```

   That's the exact message you get if you remove `NetworkKoinModule::class` from the app's `includes`.
   We tried it while building the template.

The graph is only complete in the app, which is why library modules print
`compile-safety validation skipped — no Koin entry point in this compilation`. That line is expected,
not a problem.

No KSP, no generated source folder, no reflection at runtime.

---

## 4. Following one request through the graph

What happens when the posts screen needs its data:

```
MainActivity
 └─ RouterConfiguration: koin.getAll<ModuleRouter>()
     └─ PostsModuleRouter  ← needs PostsRepository        (PostsKoinModule.moduleRouter)
         └─ PostsRepositoryImpl ← needs NetworkClient      (PostsKoinModule.repository)
             └─ KtorNetworkClient ← needs NetworkConfig, AppLogger   (NetworkKoinModule)
                 ├─ NetworkConfig ← needs AppConfig        (AppKoinModule.networkConfig)
                 │   └─ AppConfig                          (declared at startKoin)
                 └─ AppLogger ← needs LoggerConfig         (LoggerKoinModule)
                     └─ LoggerConfig ← needs AppConfig     (AppKoinModule.loggerConfig)
```

Koin resolves this bottom-up, once. After that, every `get()` returns the same instances.

Then the router takes over, **without Koin**:

```kotlin
entry<PostsRoute> {
    PostsScreen(viewModel = viewModel { PostsViewModel(repository) })
}
```

The view model is built by a plain constructor call, from the repository the router was given. This is
deliberate: see section 6.

---

## 5. The parts that need a second look

### `binds`: one definition, two types

```kotlin
@Single(binds = [ModuleRouter::class])
internal fun moduleRouter(...): PostsModuleRouter = ...
```

This registers the object as `PostsModuleRouter` *and* as `ModuleRouter`. The app then collects every
feature's router without naming any of them:

```kotlin
val moduleRouters: List<ModuleRouter> = koin.getAll()
```

Each router has its own primary type, so they don't collide. If every feature returned plain
`ModuleRouter`, they would all be the same definition and override one another.

### `internal` definitions

`PostsRepository` is `internal` to `posts-impl`, so the function providing it must be `internal` too. Koin
generates code inside the same module, so it can see it, while other modules can't. The DI graph
respects module boundaries.

### Values from outside the graph: `@Provided`

`AppConfig` comes from `BuildConfig` at startup, not from a module. It enters through the runtime DSL at
`startKoin` (`module { single { config } }`), and where it's used it's marked `@Provided`:

```kotlin
@Single
fun networkConfig(@Provided config: AppConfig): NetworkConfig = …
```

`@Provided` tells the validator "this exists at runtime, don't look for a definition". Use it sparingly:
anything marked `@Provided` is back to being checked only at runtime.

### Cross-feature dependencies

`counter` opens `posts`, so its module asks for `PostsApi`, the interface from `posts-api`:

```kotlin
@Single(binds = [ModuleRouter::class])
fun moduleRouter(postsApi: PostsApi, logger: AppLogger): CounterModuleRouter = …
```

`counter-impl` never sees `PostsApiImpl`. Koin supplies it because the app includes both modules. Gradle
(the architecture guard) forbids the impl-to-impl dependency, and Koin connects the two at the app level.

---

## 6. Rules this template follows

1. **Classes carry no DI annotations.** Repositories, view models and data sources are plain Kotlin with
   constructor parameters. Only `<Name>KoinModule` knows Koin exists. Tests build everything directly
   with fakes; no Koin is needed in a unit test.
2. **Nobody calls `get()` except the app's bootstrap.** Feature code never touches the container. That
   would hide dependencies from the validator and from the reader.
3. **View models are built by the `ModuleRouter`, not resolved from Koin.** Navigation 3 gives each
   back-stack entry its own `ViewModelStore`, and `viewModel { … }` inside the entry puts the view model
   there. It lives as long as the screen does, and its dependencies are plain constructor arguments.
4. **One `@Module` per `-impl`, included once, in the app.** `./gradlew doctor` checks every feature's
   module is in the `includes` list, and `./gradlew newFeature` adds it for you.

---

## 7. Everyday tasks

**Give a repository a new dependency.** Add the constructor parameter, then add the same parameter to its
`@Single` function:

```kotlin
@Single
internal fun repository(client: NetworkClient, logger: AppLogger): PostsRepository =
    PostsRepositoryImpl(client, logger)
```

If nothing provides the new type, the build tells you.

**Add a new core capability** (for example, storage):

1. `storage-api`: the interface. `storage-impl`: the implementation plus `@Module class StorageKoinModule`
   with a `@Single fun store(...): KeyValueStore`.
2. Add `storage-impl` to `app/build.gradle.kts`, and `StorageKoinModule::class` to the app's `includes`.
3. Features depend on `storage-api` and add a `KeyValueStore` parameter where they need it.

**A new instance on every request** (rare here): use `@Factory` instead of `@Single`.

**Two definitions of the same type** (for example, two `HttpClient`s): give them names, so the validator
and the reader both know which is which:

```kotlin
@Single @Named("auth") fun authClient(): HttpClient = …
@Single @Named("public") fun publicClient(): HttpClient = …

@Single fun api(@Named("auth") client: HttpClient): AuthApi = …
```

---

## 8. All the annotations, and which ones this template uses

`@Single` is the only one the template needs today, but Koin 4.2.2 ships the full set:

| Annotation | What it does | Here |
| --- | --- | --- |
| `@Single` / `@Singleton` | one instance for the whole app, created on first use (`createdAtStart = true` builds it at startup). They're synonyms | **everything** |
| `@Factory` | a new instance on every request | not yet: use it for cheap, stateless helpers (mappers, formatters) that shouldn't be shared |
| `@Scoped` + `@Scope(X::class)` | one instance per open *scope*, shared inside it and dropped when the scope closes | not yet: the natural fit for "per logged-in session" state (close the scope on logout) |
| `@KoinViewModel` | provides a `ViewModel`, retrieved with `koinViewModel()` | **deliberately not used**: view models are built by each `ModuleRouter`, see section 6 |
| `@Named("x")` / `@Qualifier` | tells two definitions of one type apart | not yet |
| `@InjectedParam` | a parameter the caller passes at `get { parametersOf(…) }` time, not resolved from the graph | not used: runtime values (like a post id) go through the route and the view model constructor |
| `@Property("key")` | a value from Koin properties | not used: flavor settings come from `AppConfig` |
| `@Provided` | exists at runtime; skip compile-time validation | `AppConfig` |
| `@Module(includes = …)` | groups and composes definitions | every `<Name>KoinModule`, and the app's root module |
| `@ComponentScan("pkg")` | picks up annotated *classes* in a package automatically | not used: we annotate functions, so classes stay free of Koin |
| `@Configuration` / `@KoinApplication(configurations = …)` | auto-discovers modules by label instead of listing them | not used: the explicit `includes` list is the one place that says what exists |

Two of the "not used" rows are design choices, not gaps:

- **No class annotations** (`@Single class Foo` + `@ComponentScan`). They're shorter, but every class
  would import Koin, and the wiring would spread across files instead of sitting in one module per
  capability.
- **No `@KoinViewModel`.** It works, but the view model would then be resolved from the container
  instead of being built with its route's arguments. Passing `postId` would need `@InjectedParam` and
  `parametersOf`, which move a compile-time-checked constructor call to a runtime lookup.

---

## 9. When the build fails

| Message | Meaning | Fix |
| --- | --- | --- |
| `[KOIN-D001] Missing dependency: X` | something asks for `X` and no included module provides it | add a `@Single` for `X`, or include the module that has one |
| `Koin compiler plugin: Kotlin … is not among the verified versions` (warning) | Kotlin was bumped past what the plugin supports | move Kotlin and `koinCompilerPlugin` together in `libs.versions.toml` |
| `The Koin Compiler plugin is missing` (at runtime) | a module uses the plugin's API without the plugin applied | apply `modular.di`; `modular.feature.impl` already does |
| `compile-safety validation skipped` (warning, library modules) | expected; only the app has the full graph | nothing |

And the one Koin can't catch: **a feature module missing from `includes`**. Nothing asks for its types
directly, so the graph is still "complete"; the feature just has no screens. That's what
`./gradlew doctor` is for.

---

## 10. Cheat sheet

```kotlin
@Module                                   // a group of definitions
@Module(includes = [A::class, B::class])  // compose modules
@Single                                   // one instance, created lazily
@Single(binds = [Iface::class])           // also available as Iface (for getAll)
@Factory                                  // a new instance every time
@Scoped + @Scope(Session::class)          // one instance per open scope
@Named("x")                               // tell two definitions of one type apart
@Provided                                 // supplied at startKoin; skip validation
@KoinApplication(modules = [Root::class]) // the graph's root, validated at compile time
startKoin<App> { … }                      // start it, once, in Application.onCreate
koin.get<T>() / koin.getAll<T>()          // resolve, in the app's bootstrap only
```
