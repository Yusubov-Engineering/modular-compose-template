# Kotlin & Compose — a recap for Flutter developers

Everything you need to read and change this template, in plain terms. Each idea is shown next to its
Dart/Flutter equivalent, then tied to where the template uses it.

---

## Part 1 — Kotlin

### 1. Variables

```kotlin
val name = "Ada"        // final String name = 'Ada';   — cannot be reassigned
var count = 0           // var count = 0;
const val MAX = 10      // const MAX = 10;               — top level or in an object only
```

Prefer `val`. Types are inferred; write them when it helps the reader: `val ids: List<Int> = …`.

### 2. Null safety

Same idea as Dart's sound null safety, same symbols:

```kotlin
var title: String? = null
title?.length            // null if title is null
title ?: "Untitled"      // Dart's ??
title!!.length           // Dart's !  — throws if null. Avoid.
```

`if (title != null) title.length` works without `!!`: Kotlin *smart-casts* after a check, like Dart's
type promotion.

### 3. Functions

```kotlin
fun greet(name: String, excited: Boolean = false): String =
    if (excited) "Hi, $name!" else "Hi, $name"

greet("Ada", excited = true)   // named arguments — no `required`, no braces
```

- Default values replace most overloads.
- `if` and `when` are *expressions*: they return a value.
- A single-expression function uses `=` instead of `{ return … }`.

### 4. Classes

```kotlin
class Greeter(private val name: String) {   // constructor + field in one line
    fun greet() = "Hi, $name"
}
```

Classes are **final by default**. You have to write `open` or `abstract` to allow subclasses, which is
the opposite of Dart.

| Kotlin | Dart | Used here for |
| --- | --- | --- |
| `data class Post(val id: Int, val title: String)` | a class with `==`, `hashCode`, `copyWith`, `toString` (freezed) | state, DTOs, models |
| `post.copy(title = "New")` | `post.copyWith(title: 'New')` | updating state |
| `object Logger { … }` | a singleton | `AppLogger.Silent`, `AppTheme` |
| `companion object { fun light() = … }` | `static` members / factory constructors | `AppColors.light()` |
| `interface` | `abstract interface class` | every `-api` contract |
| `enum class` | `enum` | `AppButtonVariant` |

### 5. Sealed types and `when`

The most useful Kotlin feature for app code. It works like Dart 3's `sealed class` with an exhaustive
`switch`:

```kotlin
sealed interface PostsEvent {
    data object Retry : PostsEvent
    data class PostClicked(val postId: Int) : PostsEvent
}

when (event) {                       // no `else` needed: the compiler knows every case
    PostsEvent.Retry -> load()
    is PostsEvent.PostClicked -> open(event.postId)   // `is` smart-casts `event`
}
```

Add a case and every `when` that forgot it stops compiling. The template uses this for events, effects,
`AppResult`, `NetworkError` and `PostsFailure`.

### 6. Visibility

| Kotlin | Meaning | Dart analogue |
| --- | --- | --- |
| `public` (default) | everyone | public |
| `internal` | **only this Gradle module** | a file under `lib/src/`, with `implementation_imports` as an error |
| `private` | this file or class | `_name` |

`internal` is what makes an `-impl` module sealed: routes, view models and repositories are `internal`,
so no other module can even name them.

### 7. Lambdas and extension functions

```kotlin
val double = { x: Int -> x * 2 }
list.map { it * 2 }                  // `it` is the single parameter
list.filter { post -> post.id > 3 }

fun String.shout() = uppercase() + "!"   // an extension, like Dart's `extension on String`
"hi".shout()
```

**Trailing lambdas**: when the last parameter is a function, it moves outside the parentheses. That's
why Compose code looks like `Column { … }` and `AppButton(text = "Go", onClick = { … })`.

A lambda *with a receiver*, `Type.() -> Unit`, runs as if it were inside `Type`, so `this` is that type.
`ModuleRouter.entries()` is one: inside it you call `entry<…> {}` directly.

`viewModel::dispatch` is a *function reference*, the same as passing `viewModel.dispatch` in Dart.

### 8. Scope functions

These come up everywhere; each runs a block with an object at hand:

```kotlin
val client = HttpClient().apply { expectSuccess = true }   // configure, return the object
val length = name?.let { it.length }                      // run only if not null, return the result
with(router) { registerRoutes() }                          // call several things on one object
```

### 9. Generics and `reified`

```kotlin
interface NetworkClient {
    suspend fun <T> get(path: String, response: DeserializationStrategy<T>): AppResult<T>
}
suspend inline fun <reified T> NetworkClient.get(path: String) = get(path, serializer<T>())
```

Generics are erased at runtime, like in Java. `inline` + `reified` keeps the type at the call site, so
`client.get<List<PostDto>>("posts")` can find the right serializer. `out T` in
`AppResult<out T>` means "only produces T", so an `AppResult<Nothing>` fits anywhere.

### 10. Coroutines: Kotlin's async/await

| Dart | Kotlin |
| --- | --- |
| `Future<T> f() async` | `suspend fun f(): T` |
| `await f()` | just `f()`: calling a suspend function *is* the await |
| `Future.wait([...])` | `coroutineScope { listOf(async { a() }, async { b() }).awaitAll() }` |
| `Stream<T>` | `Flow<T>` |
| `ValueNotifier` / a stream with a current value | `StateFlow<T>` |
| a subscription you must cancel | a coroutine in a *scope*, cancelled with the scope |

Suspend functions can only be called from another suspend function or from inside a **scope**:

```kotlin
viewModelScope.launch {           // cancelled automatically when the ViewModel is cleared
    val posts = repository.posts()
    emit(PostsState(posts = posts))
}
```

This is *structured concurrency*: a coroutine never outlives its scope, so there is nothing to forget to
cancel. `Dispatchers.Main` / `IO` choose the thread; Ktor and Room switch threads for you.

`CancellationException` is how cancellation travels. Never swallow it: `KtorNetworkClient` catches it
first and rethrows it.

### 11. Exceptions

Kotlin has no checked exceptions. The template's rule: **a repository throws only its feature's sealed
failure**, which the view model catches by that type (`catch (failure: PostsFailure)`). The network layer
throws nothing and returns `AppResult` values instead.

---

## Part 2 — Jetpack Compose

### 12. Composables are Flutter widgets written as functions

```kotlin
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    AppText(text = "Hello, $name", modifier = modifier)
}
```

- There's no `build` method and no widget class. The function *is* the widget.
- A composable **emits** UI; it doesn't return it (it returns `Unit`).
- Names are PascalCase, because they behave like types.
- **Recomposition** is Compose's rebuild: when state a composable read changes, Compose calls it again.
  Keep composables fast and free of side effects, like `build`.

| Flutter | Compose |
| --- | --- |
| `Column`, `Row`, `Stack` | `Column`, `Row`, `Box` |
| `Padding`, `SizedBox`, `Container`, `GestureDetector` | **`Modifier`**: `.padding()`, `.size()`, `.background()`, `.clickable()` |
| `ListView.builder` | `LazyColumn { items(list) { … } }` |
| `Expanded` | `Modifier.weight(1f)` |
| `LayoutBuilder` | `BoxWithConstraints` (see `AppAdaptiveLayout`) |
| `MediaQuery.padding` / `SafeArea` | `WindowInsets` (see `AppScaffold`) |
| `Theme.of(context)` | `AppTheme.colors` (a `CompositionLocal`) |
| `InheritedWidget` / `Provider` | `CompositionLocal` |
| `StatefulWidget` + `setState` | `remember { mutableStateOf(…) }` |
| `initState` / `dispose` | `LaunchedEffect` / `DisposableEffect` |
| `AnimatedSwitcher`, implicit animations | `AnimatedContent`, `animate*AsState` |

### 13. Modifiers

Most of what Flutter does with wrapper widgets, Compose does with a **chain**, read top to bottom:

```kotlin
Modifier
    .fillMaxWidth()
    .padding(AppTheme.spacing.lg)        // padding outside the background…
    .background(AppTheme.colors.surface)
    .padding(AppTheme.spacing.sm)        // …and inside it
```

Order matters: `.padding().background()` and `.background().padding()` look different. Every reusable
composable takes a `modifier: Modifier = Modifier` parameter and applies it to its root. The detekt
Compose rules enforce this.

### 14. State

```kotlin
var expanded by remember { mutableStateOf(false) }
AppButton(text = "Toggle", onClick = { expanded = !expanded })
```

- `mutableStateOf` is an observable value: reading it subscribes, writing it recomposes the readers.
- `remember` keeps it across recompositions. Without it, the value resets on every rebuild.
- `rememberSaveable` also survives rotation and process death.

**State hoisting.** A reusable composable takes its value and an `onChange` callback instead of owning
the state, the same as a controlled `TextField` in Flutter. Screen state lives in the view model:

```kotlin
val state by viewModel.state.collectAsStateWithLifecycle()   // StateFlow -> Compose state
PostsContent(state = state, onEvent = viewModel::dispatch)  // state down, events up
```

### 15. Side effects

A composable can run many times, so side effects go in effect handlers:

| Handler | Runs | Like |
| --- | --- | --- |
| `LaunchedEffect(key) { … }` | a coroutine when it enters, restarted when `key` changes | `initState` + async work |
| `DisposableEffect(key) { onDispose { … } }` | setup/teardown | `initState` / `dispose` |
| `rememberUpdatedState(value)` | keeps a long-lived effect reading the latest lambda | — |

`CollectEffects` in `core/statemanager` is a `LaunchedEffect` that collects a view model's effects only
while the screen is visible.

### 16. CompositionLocal: `static` vs dynamic

`CompositionLocal` passes a value down the tree without parameters, like an `InheritedWidget`.

- `staticCompositionLocalOf`: when the value changes, **the whole subtree recomposes**, but reads are
  free. Use it for things that rarely change. The design system's tokens and `LocalAppNavigator` are static.
- `compositionLocalOf` (dynamic): only readers recompose, but each read is tracked. Use it for values that
  change often.

Keep locals rare. `detekt.yml` has an allow-list, and a new one needs a reason.

### 17. ViewModel

An AndroidX `ViewModel` outlives rotation and dies when its screen leaves the back stack. It owns
`viewModelScope`. In this template, a view model:

- extends `AppStateViewModel<State, Event, Effect>`;
- takes its dependencies as constructor parameters, with no DI inside;
- never holds a `Context`, a navigator or anything from Compose.

That is what lets a plain JUnit test create one with fakes and drive it (see `PostsViewModelTest`).

### 18. Navigation 3 in one paragraph

The back stack is a plain observable list of keys. Pushing is `add`, popping is `removeLast`. `NavDisplay`
shows the top key by asking an *entry provider* which composable belongs to that key's type. Keys are
`@Serializable`, so the list can be saved and restored after process death. The template wraps this: keys
are `AppRoute`s, each feature contributes its keys and entries through a `ModuleRouter`, and screens move
around with `LocalAppNavigator.current.push(…)`.

---

## Part 3 — Gradle in five minutes

- **A module** is a folder with a `build.gradle.kts`. It's the unit of `internal` and of dependency rules,
  like a Dart package.
- **`implementation(project(":x"))`** makes a module visible to this one only.
  **`api(project(":x"))`** also passes it on to this module's consumers. Use `api` only when your public
  signatures mention the other module's types (for example, `router-api` exposes `NavKey`).
- **The version catalog**, `gradle/libs.versions.toml`, is the single `pubspec` for versions:
  `libs.ktor.client.core` in a build file refers to it.
- **Convention plugins** in `build-logic/` are shared build configuration. A module's build file says
  `id("modular.feature.impl")` and gets SDK levels, Compose, Koin, detekt and test dependencies in one line.
- **Flavors** (`dev`, `prod`) are Android's version of Flutter flavors. Build types (`debug`, `release`)
  are separate, so the app has four variants: `devDebug`, `devRelease`, `prodDebug`, `prodRelease`.
- **Configuration cache** is on: Gradle caches the configured build, which makes the second run of any
  command much faster.

---

## Where to look next

| To learn… | Read |
| --- | --- |
| a complete feature | `features/posts/posts-impl` |
| cross-feature navigation | `features/counter/counter-impl/.../CounterScreen.kt` |
| a design-system component | `core/designsystem/.../component/AppButton.kt` |
| the composition root | `app/.../bootstrap/DependencyInjectionConfiguration.kt` |
| how Koin wires it together | [koin.md](koin.md) |
| testing a view model | `features/posts/posts-impl/src/test/…/PostsViewModelTest.kt` |
| the architecture rules | [CLAUDE.md](../CLAUDE.md) |
