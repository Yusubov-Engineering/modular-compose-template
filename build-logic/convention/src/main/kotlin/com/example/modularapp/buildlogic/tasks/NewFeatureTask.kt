package com.example.modularapp.buildlogic.tasks

import com.example.modularapp.buildlogic.BASE_PACKAGE
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * `./gradlew newFeature --name=user-profile`
 *
 * Writes features/user-profile/user-profile-api and -impl — facade, launcher,
 * route, route table, Koin module, view model, screen and a test — and wires
 * them in: the app's dependencies and the app's Koin includes. settings.gradle.kts
 * finds the new modules on its own.
 *
 * The wiring is the point. A feature that exists but is not registered
 * compiles fine and has no screens.
 */
@DisableCachingByDefault(because = "Writes source files into the project.")
abstract class NewFeatureTask : DefaultTask() {
    @get:Input
    @get:Option(option = "name", description = "The feature name, kebab-case: profile, user-profile.")
    abstract val featureName: Property<String>

    @get:Internal
    abstract val rootDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val root = rootDirectory.get().asFile
        val name = FeatureName(featureName.get())
        val featureDir = root.resolve("features/${name.kebab}")
        if (featureDir.exists()) throw GradleException("features/${name.kebab} already exists.")

        writeApi(featureDir.resolve("${name.kebab}-api"), name)
        writeImpl(featureDir.resolve("${name.kebab}-impl"), name)
        wireIntoApp(root, name)

        logger.lifecycle(
            """
            Created features/${name.kebab}/${name.kebab}-api and -impl, and wired them into the app.
            Next: sync Gradle, then launch it from another feature with
              navigator.push(${name.camel}Api.launcher.${name.camel}())
            and run ./gradlew doctor.
            """.trimIndent(),
        )
    }

    private fun writeApi(dir: File, name: FeatureName) {
        val pkg = "$BASE_PACKAGE.features.${name.packageSegment}"
        dir.write("build.gradle.kts", "plugins {\n    id(\"modular.feature.api\")\n}\n")
        dir.write("consumer-rules.pro", "")
        dir.write(
            "src/main/kotlin/${pkg.asPath()}/${name.pascal}Api.kt",
            """
            package $pkg

            import $BASE_PACKAGE.core.router.AppRoute

            /** Everything other modules may use from the ${name.kebab} feature. */
            interface ${name.pascal}Api {
                val launcher: ${name.pascal}Launcher
            }

            /** The places this feature can be entered at. */
            interface ${name.pascal}Launcher {
                fun ${name.camel}(): AppRoute
            }
            """,
        )
    }

    @Suppress("LongMethod") // One template per file; splitting it up would only scatter them.
    private fun writeImpl(dir: File, name: FeatureName) {
        val apiPkg = "$BASE_PACKAGE.features.${name.packageSegment}"
        val pkg = "$apiPkg.impl"
        val src = "src/main/kotlin/${pkg.asPath()}"
        val p = name.pascal

        dir.write(
            "build.gradle.kts",
            """
            plugins {
                id("modular.feature.impl")
            }

            dependencies {
                implementation(project(":features:${name.kebab}:${name.kebab}-api"))
            }
            """,
        )
        dir.write("consumer-rules.pro", "")
        dir.write(
            "src/main/res/values/strings.xml",
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="${name.snake}_title">$p</string>
                <string name="${name.snake}_back">Back</string>
                <string name="${name.snake}_taps">Tapped %1${'$'}d times</string>
                <string name="${name.snake}_tap">Tap</string>
            </resources>
            """,
        )
        dir.write(
            "$src/router/${p}Route.kt",
            """
            package $pkg.router

            import $BASE_PACKAGE.core.router.AppRoute
            import kotlinx.serialization.Serializable

            @Serializable
            internal data object ${p}Route : AppRoute
            """,
        )
        dir.write(
            "$src/di/${p}ApiImpl.kt",
            """
            package $pkg.di

            import $BASE_PACKAGE.core.router.AppRoute
            import $apiPkg.${p}Api
            import $apiPkg.${p}Launcher
            import $pkg.router.${p}Route

            internal class ${p}ApiImpl : ${p}Api {
                override val launcher: ${p}Launcher = ${p}LauncherImpl()
            }

            internal class ${p}LauncherImpl : ${p}Launcher {
                override fun ${name.camel}(): AppRoute = ${p}Route
            }
            """,
        )
        dir.write(
            "$src/${p}ModuleRouter.kt",
            """
            package $pkg

            import androidx.lifecycle.viewmodel.compose.viewModel
            import androidx.navigation3.runtime.EntryProviderScope
            import androidx.navigation3.runtime.NavKey
            import $BASE_PACKAGE.core.router.ModuleRouter
            import $pkg.${name.packageSegment}.${p}Screen
            import $pkg.${name.packageSegment}.${p}ViewModel
            import $pkg.router.${p}Route
            import kotlinx.serialization.modules.PolymorphicModuleBuilder
            import kotlinx.serialization.modules.subclass

            /** The ${name.kebab} route table, and where its view models are built. */
            class ${p}ModuleRouter internal constructor() : ModuleRouter {

                override fun PolymorphicModuleBuilder<NavKey>.registerRoutes() {
                    subclass(${p}Route::class)
                }

                override fun EntryProviderScope<NavKey>.entries() {
                    entry<${p}Route> {
                        ${p}Screen(viewModel = viewModel { ${p}ViewModel() })
                    }
                }
            }
            """,
        )
        dir.write(
            "$src/${p}KoinModule.kt",
            """
            package $pkg

            import $BASE_PACKAGE.core.router.ModuleRouter
            import $apiPkg.${p}Api
            import $pkg.di.${p}ApiImpl
            import org.koin.core.annotation.Module
            import org.koin.core.annotation.Single

            @Module
            class ${p}KoinModule {
                @Single
                fun ${name.camel}Api(): ${p}Api = ${p}ApiImpl()

                @Single(binds = [ModuleRouter::class])
                fun moduleRouter(): ${p}ModuleRouter = ${p}ModuleRouter()
            }
            """,
        )
        dir.write(
            "$src/${name.packageSegment}/${p}ViewModel.kt",
            """
            package $pkg.${name.packageSegment}

            import $BASE_PACKAGE.core.statemanager.AppStateViewModel

            internal data class ${p}State(val taps: Int = 0)

            internal sealed interface ${p}Event {
                data object Tapped : ${p}Event
            }

            internal class ${p}ViewModel :
                AppStateViewModel<${p}State, ${p}Event, Nothing>(${p}State()) {

                override fun onEvent(event: ${p}Event) {
                    when (event) {
                        ${p}Event.Tapped -> update { it.copy(taps = it.taps + 1) }
                    }
                }
            }
            """,
        )
        dir.write(
            "$src/${name.packageSegment}/${p}Screen.kt",
            """
            package $pkg.${name.packageSegment}

            import androidx.compose.foundation.layout.Arrangement
            import androidx.compose.foundation.layout.Column
            import androidx.compose.foundation.layout.fillMaxSize
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.getValue
            import androidx.compose.ui.Alignment
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.res.stringResource
            import androidx.lifecycle.compose.collectAsStateWithLifecycle
            import $BASE_PACKAGE.core.designsystem.component.AppButton
            import $BASE_PACKAGE.core.designsystem.component.AppScaffold
            import $BASE_PACKAGE.core.designsystem.component.AppText
            import $BASE_PACKAGE.core.designsystem.component.AppTopBar
            import $BASE_PACKAGE.core.designsystem.theme.AppTheme
            import $BASE_PACKAGE.core.router.LocalAppNavigator
            import $pkg.R

            @Composable
            internal fun ${p}Screen(viewModel: ${p}ViewModel) {
                val navigator = LocalAppNavigator.current
                val state by viewModel.state.collectAsStateWithLifecycle()

                AppScaffold(
                    topBar = {
                        AppTopBar(
                            title = stringResource(R.string.${name.snake}_title),
                            onBack = if (navigator.canPop) navigator::pop else null,
                            backLabel = stringResource(R.string.${name.snake}_back),
                        )
                    },
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AppText(stringResource(R.string.${name.snake}_taps, state.taps))
                        AppButton(
                            text = stringResource(R.string.${name.snake}_tap),
                            onClick = { viewModel.dispatch(${p}Event.Tapped) },
                        )
                    }
                }
            }
            """,
        )
        dir.write(
            "src/test/kotlin/${pkg.asPath()}/${p}ViewModelTest.kt",
            """
            package $pkg

            import $pkg.${name.packageSegment}.${p}Event
            import $pkg.${name.packageSegment}.${p}ViewModel
            import org.junit.Assert.assertEquals
            import org.junit.Test

            class ${p}ViewModelTest {
                @Test
                fun `tapping counts taps`() {
                    val viewModel = ${p}ViewModel()

                    viewModel.dispatch(${p}Event.Tapped)

                    assertEquals(1, viewModel.state.value.taps)
                }
            }
            """,
        )
    }

    private fun wireIntoApp(root: File, name: FeatureName) {
        val path = ":features:${name.kebab}:${name.kebab}"
        root.resolve("app/build.gradle.kts").insertBefore(
            anchor = "// <generated:feature-dependencies>",
            lines = listOf("implementation(project(\"$path-api\"))", "implementation(project(\"$path-impl\"))"),
        )

        val di = root.resolve("app/src/main/kotlin/${BASE_PACKAGE.asPath()}/bootstrap/DependencyInjectionConfiguration.kt")
        di.insertBefore(anchor = "// <generated:koin-modules>", lines = listOf("${name.pascal}KoinModule::class,"))
        di.addImport("$BASE_PACKAGE.features.${name.packageSegment}.impl.${name.pascal}KoinModule")
    }

    private fun File.write(relative: String, content: String) {
        val file = resolve(relative)
        file.parentFile.mkdirs()
        file.writeText(if (content.isEmpty()) "" else content.trimIndent().trimStart() + "\n")
    }

    /** Inserts [lines] above the line holding [anchor], at the anchor's indentation. */
    private fun File.insertBefore(anchor: String, lines: List<String>) {
        val text = readText()
        val anchorLine = text.lines().firstOrNull { anchor in it }
            ?: throw GradleException("$name lost its '$anchor' anchor; add the lines by hand: $lines")
        val indent = anchorLine.takeWhile(Char::isWhitespace)
        writeText(text.replace(anchorLine, lines.joinToString("") { "$indent$it\n" } + anchorLine))
    }

    /** Adds an import and keeps the import block sorted. */
    private fun File.addImport(fqName: String) {
        val lines = readLines()
        val imports = lines.filter { it.startsWith("import ") }
        val sorted = (imports + "import $fqName").distinct().sorted()
        val first = lines.indexOfFirst { it.startsWith("import ") }
        val rest = lines.filterNot { it.startsWith("import ") }.toMutableList()
        rest.addAll(first, sorted)
        writeText(rest.joinToString("\n") + "\n")
    }

    private fun String.asPath(): String = replace('.', '/')
}
