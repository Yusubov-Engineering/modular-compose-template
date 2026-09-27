import com.android.build.api.dsl.CommonExtension
import com.example.modularapp.buildlogic.lib
import com.example.modularapp.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
 * Adds Compose to an Android module that already applied a base plugin.
 *
 * `./gradlew assembleDebug -PcomposeReports` writes the Compose compiler's
 * stability reports to <module>/build/compose-reports: which classes are
 * stable, and which composables are restartable and skippable. Read
 * `*-composables.txt` first.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.getByType(CommonExtension::class.java).buildFeatures.compose = true

        extensions.configure<ComposeCompilerGradlePluginExtension> {
            stabilityConfigurationFiles.add(
                rootProject.layout.projectDirectory.file("config/compose/stability.conf"),
            )
            if (providers.gradleProperty("composeReports").isPresent) {
                val reports = layout.buildDirectory.dir("compose-reports")
                reportsDestination.set(reports)
                metricsDestination.set(reports)
            }
        }

        dependencies {
            val bom = platform(libs.lib("compose-bom"))
            add("implementation", bom)
            add("implementation", libs.lib("compose-runtime"))
            add("implementation", libs.lib("compose-foundation"))
            add("implementation", libs.lib("compose-ui"))
            add("implementation", libs.lib("compose-ui-tooling-preview"))
            add("debugImplementation", libs.lib("compose-ui-tooling"))
        }
    }
}
