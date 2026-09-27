import com.android.build.api.dsl.ApplicationExtension
import com.example.modularapp.buildlogic.addUnitTestDependencies
import com.example.modularapp.buildlogic.applyArchitectureGuard
import com.example.modularapp.buildlogic.configureKotlinAndroid
import com.example.modularapp.buildlogic.libs
import com.example.modularapp.buildlogic.version
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.util.Properties

/**
 * The app module: Compose, both flavor dimensions, and the flavor's
 * configuration turned into BuildConfig fields.
 *
 * `environment` (dev / prod) mirrors the Flutter template's flavors, and its
 * values come from config/<flavor>.properties — the counterpart of
 * config/<flavor>.json and `--dart-define-from-file`. Every key in the file
 * becomes a `BuildConfig` string field, so adding a setting needs no Gradle
 * edit. `di` (koin / dagger) comes from modular.di.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("modular.detekt")

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)
            defaultConfig.targetSdk = libs.version("android-targetSdk").toInt()
            buildFeatures.buildConfig = true

            flavorDimensions += ENVIRONMENT
            ENVIRONMENTS.forEach { (name, suffix) ->
                productFlavors.register(name) {
                    dimension = ENVIRONMENT
                    applicationIdSuffix = suffix
                    readConfig(name).forEach { (key, value) ->
                        buildConfigField("String", key, "\"$value\"")
                    }
                }
            }

            buildTypes.named("release") {
                isMinifyEnabled = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
            }
        }

        pluginManager.apply("modular.android.compose")
        pluginManager.apply("modular.di")

        applyArchitectureGuard()
        addUnitTestDependencies()
    }

    private fun Project.readConfig(flavor: String): Map<String, String> {
        val file = rootProject.layout.projectDirectory.file("config/$flavor.properties")
        val text = providers.fileContents(file).asText.orNull
            ?: throw GradleException("Missing config/$flavor.properties for the '$flavor' flavor.")
        val properties = Properties().apply { load(text.reader()) }
        return properties.stringPropertyNames().sorted().associateWith { properties.getProperty(it) }
    }

    private companion object {
        const val ENVIRONMENT = "environment"

        /** Flavor name to application id suffix. prod ships unsuffixed. */
        val ENVIRONMENTS = listOf("dev" to ".dev", "prod" to null)
    }
}
