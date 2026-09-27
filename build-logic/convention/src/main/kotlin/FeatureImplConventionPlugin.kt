import com.example.modularapp.buildlogic.lib
import com.example.modularapp.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * A feature's `-impl` module: screens, view models, data, its route table
 * and its Koin module.
 *
 * Everything a feature usually needs is added here, so a feature's own build
 * file lists only what is particular to it: its own `-api`, and the `-api`
 * of any other feature it launches.
 */
class FeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("modular.android.library")
        pluginManager.apply("modular.android.compose")
        pluginManager.apply("modular.di")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        dependencies {
            add("implementation", project(":core:designsystem"))
            add("implementation", project(":core:statemanager"))
            add("implementation", project(":core:router:router-api"))
            add("implementation", project(":core:logger:logger-api"))
            add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
            add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
            add("implementation", libs.lib("androidx-navigation3-runtime"))
            add("implementation", libs.lib("kotlinx-serialization-json"))
        }
    }
}
