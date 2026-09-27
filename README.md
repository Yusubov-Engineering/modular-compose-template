# Modular Compose Template

An Android starter for apps that outgrow one `app` module. It is the Jetpack Compose counterpart of
[`modular_app_template`](https://github.com/Yusubov-Engineering/modular_app_template) (Flutter): the same
`-api` / `-impl` split, the same launcher-based navigation, and the same State / Event / Effect screens,
built from native pieces.

| Concern | Flutter template | Here |
| --- | --- | --- |
| Module boundaries | pub workspace + `implementation_imports` | Gradle modules + `internal` + an architecture guard |
| Shared build setup | `analysis_options` / melos | convention plugins in `build-logic/` |
| DI | `DependencyModule` over get_it | Koin, checked at compile time by its K2 compiler plugin |
| Navigation | `router_api` over go_router | `router-api` over Navigation 3 |
| State | `AppStateController<S, E, F>` | `AppStateViewModel<S, E, F>` over AndroidX ViewModel |
| Network | `network_api` over Dio | `network-api` over Ktor (OkHttp engine) |
| Logging | `logger_api` | `logger-api` over Kermit |
| Design system | `ThemeExtension` tokens | `staticCompositionLocalOf` tokens on Compose Foundation (no Material) |
| Lint | `app_linter` | detekt + Compose rules, one shared config |
| CLI | `modular new feature`, `modular doctor` | `./gradlew newFeature`, `./gradlew doctor` |

Storage and analytics are not included yet.

## Start a project from this template

### 1. Create the repository

On GitHub, click **Use this template → Create a new repository**. Or from a terminal:

```bash
gh repo create <owner>/my-app --template Yusubov-Engineering/modular-compose-template --private --clone
cd my-app
```

The new repository starts with a single fresh commit and no link back to the template.

### 2. Open it in Android Studio

Use **File → Open**, choose the folder and let Gradle sync. Open the folder only after the clone has
finished: if Studio opens a half-copied folder, it saves a non-Gradle project setup and later reports
*"Unable to determine project Android Gradle Plugin (AGP) version"*. If that happens, close the project,
delete everything in `.idea/` except `runConfigurations/`, and open it again.

### 3. Change the package and application id

From the repository root, with your own package in `NEW`:

```bash
OLD=com.example.modularapp NEW=com.acme.myapp
OLD_DIR=${OLD//.//} NEW_DIR=${NEW//.//}

# Every text reference: Kotlin packages and imports, namespace, applicationId, build-logic, docs.
grep -rIl --exclude-dir=.git --exclude-dir=build --exclude-dir=.gradle "$OLD" . \
  | xargs sed -i '' "s/$OLD/$NEW/g"   # on Linux: sed -i "s/…/…/g"

# Move the source folders to match the new package, in every module.
find . -type d -path "*/kotlin/$OLD_DIR" -not -path "*/build/*" | while read -r d; do
  dst="${d%$OLD_DIR}$NEW_DIR"; mkdir -p "$(dirname "$dst")"; git mv "$d" "$dst"
done
find . -type d -empty -path "*/kotlin/*" -not -path "./.git/*" -delete
```

This covers the Kotlin packages, the app's `namespace` and `applicationId`, and `BASE_PACKAGE` in
`build-logic`, which every module's namespace is derived from. Sync Gradle afterwards.

### 4. Rename the app

| What | Where |
| --- | --- |
| Name shown on the device | `app/src/main/res/values/strings.xml` → `app_name` |
| Gradle project name | `settings.gradle.kts` → `rootProject.name` |
| The `app` run configuration's module | `.idea/runConfigurations/app.xml` → `<rootProject.name>.app.main`. Change it together with `rootProject.name`, or the configuration can't find the module |
| Log tag | `core/logger/logger-impl/…/LoggerConfig.kt` → `tag` |
| Class and theme names (optional, cosmetic) | `ModularApplication`, `Theme.ModularApp`, `ModularKoinApplication` |

### 5. Point it at your backend

Set `API_BASE_URL`, and any other settings, in `config/dev.properties` and `config/prod.properties`.
Every key becomes a `BuildConfig` field, which `app/…/bootstrap/AppConfig.kt` reads.

### 6. Replace the example features

1. Create your first feature: `./gradlew newFeature --name=home`.
2. Choose where the app opens: in `app/…/bootstrap/RouterConfiguration.kt`, set `initialRoute` from your
   feature's launcher, e.g. `koin.get<HomeApi>().launcher.home()`.
3. Delete `features/counter` and `features/posts`. Then remove their lines from `app/build.gradle.kts` and
   from the `includes` list in `DependencyInjectionConfiguration.kt`.

The build and `./gradlew doctor` point out anything left behind.

### 7. Verify and commit

```bash
./gradlew :app:assembleDevDebug testDebugUnitTest testDevDebugUnitTest detekt doctor
git add -A && git commit -m "Set up my-app from modular-compose-template"
```

### 8. Before the first release

- **Add release signing.** The template has none yet, so a `prodRelease` build is unsigned and Google Play
  will reject it. Read the keystore details from a gitignored `key.properties`.
- **Rewrite this README and `CLAUDE.md`** to describe your app rather than the template.

## Run it

```bash
./gradlew :app:installDevDebug      # dev flavor: .dev suffix, logs on
./gradlew :app:installProdRelease   # prod flavor (needs a signing config for a real release)
```

You get a counter screen that opens a list of posts from [JSONPlaceholder](https://jsonplaceholder.typicode.com).
Like the Flutter template, the screens themselves are simple on purpose. The point is how they are wired together.

## Check it

```bash
./gradlew testDebugUnitTest testDevDebugUnitTest   # unit tests
./gradlew detekt                                   # lint, Compose rules included
./gradlew doctor                                   # rules the compiler cannot check
```

## Android Studio

Shared run configurations live in `.idea/runConfigurations/` and are committed (the rest of `.idea/` is
ignored), so they show up in the run menu after the first Gradle sync:

| Configuration | Does |
| --- | --- |
| `app` | builds, installs and launches the app; debug with the bug icon |
| `Unit tests` | `testDebugUnitTest testDevDebugUnitTest`, shown in the test runner |
| `Detekt` | `detekt` |
| `Doctor` | `doctor` |
| `Verify (build + tests + detekt + doctor)` | everything CI should run |

`app` runs whichever variant is selected in **View → Tool Windows → Build Variants** (`devDebug` by
default; switch the `app` row to `prodDebug` for prod). A configuration made through **Run → Edit
Configurations** is personal until you tick **Store as project file**. Tick it to share the configuration
through `.idea/runConfigurations/`.

## Add a feature

```bash
./gradlew newFeature --name=user-profile
```

This writes `features/user-profile/user-profile-api` and `-impl`: facade, launcher, route, route table,
Koin module, view model, screen and a test. It also adds the feature to the app's dependencies and to its
Koin module list. `settings.gradle.kts` includes any module it finds under `core/`, `base/` and `features/`,
so you don't register modules by hand.

## Layout

```
app/                         the only leaf: composition root, flavors, MainActivity
build-logic/convention/      convention plugins + newFeature / doctor tasks
config/                      dev.properties, prod.properties -> BuildConfig; detekt.yml
core/
  designsystem/              tokens, AppTheme, components, icons (res/drawable)
  statemanager/              AppStateViewModel, CollectEffects
  logger/logger-api|impl     AppLogger over Kermit
  network/network-api|impl   NetworkClient, AppResult over Ktor
  router/router-api|impl     AppRoute, AppNavigator, ModuleRouter; AppNavHost over Navigation 3
features/
  counter/counter-api|impl   the start destination; opens posts through PostsApi
  posts/posts-api|impl       network + data layer example
docs/
  kotlin-compose-recap.md    Kotlin and Compose in plain terms, for Flutter developers
  koin.md                    how dependency injection works here, with a cheat sheet
```

## How it works

A tour of each piece in plain terms. For more depth, see [CLAUDE.md](CLAUDE.md) (the rules),
[docs/koin.md](docs/koin.md) (dependency injection) and
[docs/kotlin-compose-recap.md](docs/kotlin-compose-recap.md) (if Kotlin or Compose is new to you).

### The big picture: modules and their rules

The app is split into Gradle modules, and most capabilities come as a pair:

- **`-api`** says *what* a capability can do: interfaces only. `posts-api` has `PostsApi` and `PostsLauncher`.
- **`-impl`** says *how*: screens, view models, data, wiring. Almost everything in it is `internal`,
  so no other module can see it.

Dependencies only point downward: `app` → `features` → `core`. Features talk to each other only through an
`-api`. **Only `app` may depend on an `-impl`**. That makes `app` the *composition root*, the one place that
knows which implementations exist.

The build enforces this: a convention plugin fails the build if any other module depends on an `-impl`.
The rules the compiler can't see, such as "is every feature actually registered?", are checked by
`./gradlew doctor`.

### Dependency injection (Koin)

Classes never create what they need; they receive it through their constructor. **Koin** is the container
that builds every object once and passes it wherever it's needed.

Each `-impl` has one Koin module that says how to build its objects. It uses annotated functions, so the
classes themselves stay free of Koin:

```kotlin
@Module
class PostsKoinModule {
    @Single
    fun postsApi(): PostsApi = PostsApiImpl()

    @Single
    internal fun repository(client: NetworkClient): PostsRepository = PostsRepositoryImpl(client)
}
```

Read a function as "to provide a `PostsRepository`, I need a `NetworkClient`." The app lists every
module once, in `app/.../bootstrap/DependencyInjectionConfiguration.kt`, and starts Koin in `Application.onCreate()`.

**Koin's compiler plugin checks the whole graph while the app compiles.** If something asks for a type no
module provides, the *build* fails (`Missing dependency: NetworkClient`) rather than the app crashing later.

### Navigation (router over Navigation 3)

Navigation 3 keeps the screen history as a plain list of **routes**. The last route in the list is the
screen you see. `push` adds one, `pop` removes the last one.

- **Routes are private to their feature.** `PostDetailsRoute(postId)` is `internal` to `posts-impl`.
- **Other features get routes from a launcher.** `PostsApi.launcher.posts()` returns an `AppRoute` without
  revealing which one. The launcher only says *where*; the caller picks *how* (`push`, `replace`, `goTo`):

  ```kotlin
  navigator.push(postsApi.launcher.posts())
  ```

- **Each feature has one `ModuleRouter`** that maps its routes to screens, and registers them so the
  history can be saved. The app collects every feature's router, and `AppNavHost` shows the current screen.
- The history **survives rotation and the app being killed in the background**. Each screen gets its own
  view model, which is cleared when the screen leaves the history.

A screen gets the navigator with `LocalAppNavigator.current`.

### State management (State / Event / Effect)

Every screen follows the same loop, built on the standard Android `ViewModel`:

```
   user taps ──► Event ──► ViewModel ──► new State ──► screen redraws
                               └───────► Effect ─────► screen navigates / shows a message
```

- **State** is everything the screen draws, as one immutable object (`PostsState`). The screen observes it
  and redraws when it changes.
- **Event** is something the user did (`PostsEvent.Retry`). The screen sends it with `viewModel.dispatch(…)`.
- **Effect** is a one-time instruction back to the screen (`PostsEffect.OpenDetails`). **Navigation is always
  an effect.** The view model never holds a navigator or a `Context`, which is why a plain unit test can drive it.

```kotlin
internal class CounterViewModel(
    private val logger: AppLogger,
) : AppStateViewModel<CounterState, CounterEvent, CounterEffect>(CounterState()) {
    override fun onEvent(event: CounterEvent) {
        when (event) {
            CounterEvent.Increment -> update { it.copy(count = it.count + 1) }
            CounterEvent.OpenDetails -> emitEffect(CounterEffect.OpenDetails(currentState.count))
            // …
        }
    }
}
```

The screen reads state with `collectAsStateWithLifecycle()` and handles effects with `CollectEffects`.

### Networking (Ktor)

Features never see Ktor. They use `NetworkClient` from `network-api`:

```kotlin
val result: AppResult<List<PostDto>> = client.get("posts")
```

- **It never throws for a failed request.** Every call returns `AppResult.Success(value)` or
  `AppResult.Failure(error)`. `error` is one of a fixed set: no connection, timeout, HTTP status, bad body, unknown.
- `network-impl` builds one Ktor client (OkHttp engine) with the base URL, timeouts and logging from the
  flavor's config. Relative paths like `"posts"` resolve against that base URL.

Each layer speaks its own language:

1. the **repository** turns a network error into the feature's own failure (`PostsFailure.NoConnection`);
2. the **view model** keeps that failure in its state;
3. the **screen** turns it into translated text.

### Design system

`core/designsystem` is a small, custom design system built on Compose Foundation, without Material.

- **Tokens, not raw values.** Colours, spacing, corner radii, text styles and animation durations are
  read from the theme: `AppTheme.colors.textPrimary`, `AppTheme.spacing.lg`, `AppTheme.motion.medium`.
  `AppTheme { … }` provides them to everything inside it through `staticCompositionLocalOf`, and switches
  between light and dark automatically.
- **Components**: `AppButton`, `AppCard`, `AppText`, `AppTopBar`, `AppScaffold`, `AppProgressIndicator`,
  `AppMessageView`, `AppAdaptiveLayout`. Anything tappable is built on `AppPressable`, which handles
  accessibility, the minimum touch size and press feedback.
- **Accents recolour a whole area**: wrap it in `AppAccentScope(AppAccent.Teal) { … }`.
- **Icons are ordinary vector drawables** in `res/drawable`, shown with `AppIcon(R.drawable.ic_back, …)`.
- **It respects the system's "Remove animations" setting**: all durations become zero.

### Logging

Code logs through `AppLogger` (`debug`, `info`, `warning`, `error`). Behind it is **Kermit**, writing to
Logcat, and the network layer's HTTP logs go through the same logger. Everything is logged in `dev` and in
any debug build; a `prod` release logs errors only.

### Configuration and flavors

There are two flavors, **dev** and **prod**. Their settings live in `config/dev.properties` and
`config/prod.properties`. Every key becomes a `BuildConfig` field, which `AppConfig` reads in one place.
dev installs as `com.example.modularapp.dev`, so it can sit next to prod on the same phone.

### Build setup and quality checks

- **Convention plugins** (`build-logic/`) hold the shared build setup. A feature's build file is a few
  lines, because `id("modular.feature.impl")` brings Compose, Koin, the design system, the state manager,
  navigation, lint and the test libraries.
- **Versions** all live in `gradle/libs.versions.toml`.
- **detekt** with the Compose rules checks code style and Compose best practices, from one shared config.
- **`./gradlew doctor`** checks what the compiler can't: every feature is registered in the app, and every
  route can be saved.
- **`./gradlew newFeature --name=…`** creates a new feature, already wired in.

### Testing

View models, repositories and the network client are plain classes that receive their dependencies, so
tests build them directly with fakes:

- **network:** Ktor's `MockEngine`;
- **repositories:** a fake `NetworkClient`;
- **view models:** a fake repository.

No emulator or container is needed. Run them with `./gradlew testDebugUnitTest testDevDebugUnitTest`.

## Toolchain

Gradle 9.6 (wrapper), AGP 9.4 (built-in Kotlin), Kotlin 2.4.20, JDK 17, compileSdk / targetSdk 37,
minSdk 26. Every version is in [gradle/libs.versions.toml](gradle/libs.versions.toml). Kotlin and the Koin
compiler plugin must move together, because a compiler plugin only supports the Kotlin versions it was built for.
