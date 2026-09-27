import com.example.modularapp.buildlogic.tasks.DoctorTask
import com.example.modularapp.buildlogic.tasks.NewFeatureTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Root-only tasks — the Gradle counterpart of the Flutter template's
 * `modular` CLI:
 *
 * - `./gradlew newFeature --name=profile`: an -api/-impl pair, wired into the app.
 * - `./gradlew doctor`: the rules the compiler cannot check.
 */
class RootConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        tasks.register("newFeature", NewFeatureTask::class.java) {
            group = "modular"
            description = "Creates features/<name>/<name>-api and -impl, and wires them into the app."
            rootDirectory.set(layout.projectDirectory)
        }
        tasks.register("doctor", DoctorTask::class.java) {
            group = "modular"
            description = "Checks the architecture rules the compiler cannot."
            rootDirectory.set(layout.projectDirectory)
        }
        Unit
    }
}
