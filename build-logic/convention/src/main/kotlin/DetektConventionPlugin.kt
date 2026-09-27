import com.example.modularapp.buildlogic.lib
import com.example.modularapp.buildlogic.libs
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Detekt with one shared ruleset, config/detekt/detekt.yml, plus the Compose
 * rules. Rules change there and only there — never per module.
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.gitlab.arturbosch.detekt")

        extensions.configure<DetektExtension> {
            config.setFrom(rootProject.file("config/detekt/detekt.yml"))
            buildUponDefaultConfig = true
            parallel = true
            source.setFrom(
                "src/main/kotlin",
                "src/test/kotlin",
            )
        }

        dependencies {
            add("detektPlugins", libs.lib("detekt-compose-rules"))
        }
    }
}
