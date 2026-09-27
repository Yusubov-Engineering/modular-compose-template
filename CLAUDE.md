# CLAUDE.md

Guidance for Claude Code when working in this repository.

## What this is

A **module-per-capability Android template** in Jetpack Compose. It is the native counterpart of the
Flutter `modular_app_template`, and deliberately keeps the same shape: every capability is an `-api`
module (the contract) and an `-impl` module (the implementation). It is one Gradle build, a monorepo.
Nothing is published or pulled in from other repos.

## Commands

Run everything from the repo root.

| Task | Command |
| ---- | ------- |
| Build the app | `./gradlew :app:assembleDevDebug` |
| Install and run | `./gradlew :app:installDevDebug` |
| Unit tests | `./gradlew testDebugUnitTest testDevDebugUnitTest` |
| Lint | `./gradlew detekt` |
| Architecture rules | `./gradlew doctor` |
| New feature | `./gradlew newFeature --name=<kebab-name>` |

Library modules have `debug`/`release` variants, and the app has `dev`/`prod` × `debug`/`release`.
That is why the tests need both task names. Before claiming work is done, the build, the tests,
`detekt` and `doctor` must all pass.

## Architecture

Dependencies flow downward: `app` → `features` → `core`.

- **`app/`** is the only leaf and the composition root. It is the only module that may depend on an
  `-impl`. `bootstrap/DependencyInjectionConfiguration.kt` holds the one list of Koin modules, and
  `bootstrap/RouterConfiguration.kt` collects the routes and picks the start destination.
- **`features/<name>/<name>-api` and `-impl`** hold one business domain each. Other features depend on
  the `-api` only.
- **`core/`** is infrastructure: `designsystem`, `statemanager`, and the `logger`, `network` and `router`
  api/impl pairs.

### The rules, and what enforces them

| Rule | Enforced by |
| --- | --- |
| Only `:app` depends on an `-impl` | `ArchitectureGuard.kt`, which fails configuration of any other module that tries |
| An `-impl`'s internals are unreachable | Kotlin `internal`: routes, view models, repositories and DTOs are all `internal` |
| Every feature is registered in the app | `./gradlew doctor` (feature-not-registered, feature-not-a-dependency) |
| Every route can be saved | `./gradlew doctor` (route-not-registered) |
| The DI graph is complete | the Koin compiler plugin, when `:app` compiles `startKoin<…>()` |

### A feature's `-impl`

```
<Feature>KoinModule.kt        public: what the app includes
<Feature>ModuleRouter.kt      public class, internal constructor: routes + where view models are built
router/<Feature>Routes.kt     internal @Serializable routes. Never exported.
di/<Feature>ApiImpl.kt        the facade and the launcher
domain/  data/                models, repository contract + impl, sealed failure
<screen>/                     <Screen>ViewModel.kt, <Screen>Screen.kt
res/values/strings.xml        the feature's own strings
```

**Don't create this by hand. Run `./gradlew newFeature`**, which also does the wiring.

### Dependency injection (Koin + compiler plugin)

The full walkthrough is in [docs/koin.md](docs/koin.md).


- Each module declares `@Module class <Name>KoinModule` with `@Single` **functions** that call
  constructors. Classes themselves carry no DI annotations, so domain, data and view-model code has no
  Koin import and tests construct everything directly.
- Internal types can be provided by `internal` functions in the module (see `PostsKoinModule`).
- Each feature binds its router with `@Single(binds = [ModuleRouter::class])`, and the app collects them
  all with `koin.getAll<ModuleRouter>()`.
- Values that come from outside the graph (the flavor's `AppConfig`) are declared at `startKoin` and
  marked `@Provided` where they are used, so compile-time validation doesn't look for a definition.
- Compile safety runs only where `startKoin<…>()` is compiled (the app). Library modules log
  "compile-safety validation skipped". That is expected, not an error.
- **View models are not resolved from Koin.** The `ModuleRouter` receives dependencies through its
  constructor and builds each view model with `viewModel { FooViewModel(repo) }` inside the route's entry.
  Navigation 3's `ViewModelStore` decorator scopes it to that back-stack entry.

### Navigation (Navigation 3)

- `router-api`: `AppRoute` (a `NavKey`), `AppNavigator`, `ModuleRouter`, `LocalAppNavigator`.
  `router-impl`: `AppNavHost` (`NavDisplay`) and `BackStackNavigator`.
- **Routes are `internal` to their `-impl`.** Another feature gets a route only from the owning feature's
  launcher, typed as `AppRoute`. **The launcher returns a route and the caller picks the verb**:
  `navigator.push(postsApi.launcher.posts())`.
- Inside its own module, a screen uses the route directly: `navigator.push(PostDetailsRoute(id))`.
- **Every route must be registered twice** in its `ModuleRouter`: `subclass(Route::class)` in
  `registerRoutes()` for saving the back stack, and `entry<Route>` in `entries()` for showing it. A route
  missing from `registerRoutes()` navigates fine, then crashes the first time the stack is saved.
  `doctor` checks this.
- The back stack survives process death (verified on an emulator with `am kill`). A restored entry gets
  a new view model, so screens reload their data.
- Page transitions are set once, in `AppNavHost`, and honour reduced motion.
- `AppNavigator.pop()` returns `Unit`, so `navigator::pop` fits any `() -> Unit`. Check `canPop` to decide
  whether to show a back button.

### State (`core/statemanager`)

`AppStateViewModel<S, E, F>`: `state: StateFlow<S>`, `dispatch(event)` → `onEvent`, `emit` / `update`
for state, and `emitEffect` for one-shot effects. **Navigation is an effect.** The view model never holds
a navigator or a `Context`. The screen collects state with `collectAsStateWithLifecycle()` and effects
with `CollectEffects(viewModel.effects) { … }`. Initial loading goes in `init {}`, the equivalent of
Flutter's `onInit`. Use `Nothing` as `F` when a screen has no effects.

Effects are buffered, not dropped: one sent while the screen is stopped is delivered when it comes back.
They are delivered once, to one collector.

### Compose stability

Strong skipping is on (the Kotlin 2.x default), so every composable is skippable. Stable parameters are
compared with `equals`, and unstable ones by instance (`===`).

- **View models and launchers are passed only to the screen composable, never further down.** Each is
  the same instance for the screen's lifetime (per back-stack entry, and a Koin singleton), so the
  instance check always passes. Children get state and lambdas (`PostsContent(state, onEvent)`), which the
  Compose rules' `ViewModelForwarding` check enforces.
- **UI state classes that hold a type Compose can't prove immutable get `@Immutable`** (`PostsState`
  holds a `PostsFailure`, which is an `Exception`). State is replaced, never edited, so the promise holds.
- `config/compose/stability.conf` declares the read-only `List`/`Set`/`Map` interfaces stable for every
  module. Never add a wildcard there: `MutableList` lives in the same package.
- `./gradlew assembleDebug -PcomposeReports` writes `<module>/build/compose-reports/`. Read
  `*-composables.txt` and `*-classes.txt`.

### Network (`core/network`)

`NetworkClient.get<T>(path)` returns `AppResult<T>`, which is `Success` or `Failure(NetworkError)`.
**It never throws for a failed request**, except to propagate coroutine cancellation. The decoding uses
the caller's serializer, so there's no content negotiation and no reflection. Base URL, timeouts and
logging come from `NetworkConfig`, which the app builds from `config/<flavor>.properties`.

A feature's repository turns `NetworkError` into the feature's own sealed failure, exhaustively, and
throws only that. The view model catches exactly that type and keeps it in state as a value. The screen
turns it into a sentence with `stringResource` (see `posts-impl/shared/PostsFailureText.kt`).

### Design system (`core/designsystem`)

- **Tokens, never raw values**: `AppTheme.colors.textPrimary`, `AppTheme.spacing.lg`,
  `AppTheme.motion.medium`. Tokens are `staticCompositionLocalOf` values provided by `AppTheme {}`.
  Adding a colour means adding it to both `AppColors.light()` and `dark()`.
- **Built on Compose Foundation, not Material.** Press feedback is `AppPressIndication`, installed as
  `LocalIndication`.
- **Colour a subtree with `AppAccentScope`**, not with colours.
- **Motion honours "Remove animations"**: `AppTheme.motion` is `AppMotion.reduced()` when the animator
  scale is 0. Anything that loops must also check `motion.isReduced`.
- **Tappable things are built on `AppPressable`**, which owns button semantics, the 48dp touch target and
  press feedback.
- **Icons are vector drawables in `res/drawable`**, used through `AppIcon(R.drawable.ic_x, …)`. There's no
  icon list to maintain. Draw them in black; `AppIcon` tints them.
- `AppScaffold` owns and consumes the system insets, so bodies never pad for them again.
- `AppAdaptiveLayout` picks portrait or landscape from its own constraints (more than 1.2× wider than tall).

### Flavors and config

`config/dev.properties` and `config/prod.properties`: every key becomes a `BuildConfig` string field.
`app/.../bootstrap/AppConfig.kt` is the only place `BuildConfig` is read. dev adds a `.dev` application
id suffix; prod ships without one.

## Conventions

- Namespaces and Kotlin packages are derived from the module path:
  `:features:user-profile:user-profile-impl` becomes `com.example.modularapp.features.userprofile.impl`.
  Keep the package equal to the namespace so `R` needs no import.
- Detekt rules change only in `config/detekt/detekt.yml`, never per module. `warningsAsErrors` is on.
  When you add a design-system component that emits UI, add it to `contentEmitters` there, or the Compose
  rules won't treat it as one.
- Composables take `modifier: Modifier = Modifier` as their first optional parameter.

## Editing traps

- **Anchors.** `app/build.gradle.kts` (`// <generated:feature-dependencies>`) and
  `DependencyInjectionConfiguration.kt` (`// <generated:koin-modules>`) carry anchors that `newFeature`
  inserts above. Don't remove them.
- **The root `plugins { … apply false }` block is load-bearing.** build-logic depends on the Gradle
  plugins `compileOnly`, so the root block is what puts them on the classpath, and it's where Android
  Studio reads the AGP version during sync. Without it, sync fails with "Unable to determine project
  Android Gradle Plugin (AGP) version". A new plugin goes in the catalog's `[plugins]`, in that block,
  and in build-logic as `compileOnly`.
- **AGP 9 compiles Kotlin itself.** Don't apply `org.jetbrains.kotlin.android`. Kotlin options live on
  the `kotlin` extension, which `configureKotlinAndroid` sets.
- **Kotlin and the Koin compiler plugin move together.** A Kotlin version outside the plugin's verified
  list prints a warning and may miscompile. Check the plugin's release notes before bumping either.
- **`navigator::pop` must stay a `() -> Unit`.** An earlier `pop(): Boolean` couldn't be passed as a
  click handler.
- **`data object` failures extending `Exception`** need no hand-written `readResolve`. The compiler adds
  it.
