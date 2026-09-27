import com.example.modularapp.buildlogic.lib
import com.example.modularapp.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Koin, with Koin's own K2 compiler plugin.
 *
 * Modules declare their wiring with annotations (`@Module`, `@Single`); the
 * compiler plugin generates the definitions and, when the app compiles its
 * `startKoin<…>()`, validates the whole graph — a definition asking for
 * something nobody provides is a build error, not a crash on first use.
 * No KSP, no reflection.
 */
class DependencyInjectionConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.insert-koin.compiler.plugin")

        dependencies {
            add("implementation", platform(libs.lib("koin-bom")))
            add("implementation", libs.lib("koin-core"))
            add("implementation", libs.lib("koin-annotations"))
            add("testImplementation", libs.lib("koin-test"))
        }
    }
}
