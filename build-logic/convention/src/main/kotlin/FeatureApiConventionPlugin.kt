import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * A feature's `-api` module: the `<Feature>Api` facade and its launcher.
 *
 * `api(...)` rather than `implementation(...)` on the router contract,
 * because every launcher returns an `AppRoute`, and a consumer of the
 * launcher must be able to see that type.
 */
class FeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("modular.android.library")

        dependencies {
            add("api", project(":core:router:router-api"))
        }
    }
}
