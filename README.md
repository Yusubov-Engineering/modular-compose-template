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

See [CLAUDE.md](CLAUDE.md) for the architecture and the rules that hold it together, and
[docs/kotlin-compose-recap.md](docs/kotlin-compose-recap.md) if Kotlin or Compose is new to you.

## Toolchain

Gradle 9.6 (wrapper), AGP 9.4 (built-in Kotlin), Kotlin 2.4.20, JDK 17, compileSdk / targetSdk 37,
minSdk 26. Every version is in [gradle/libs.versions.toml](gradle/libs.versions.toml). Kotlin and the Koin
compiler plugin must move together, because a compiler plugin only supports the Kotlin versions it was built for.
