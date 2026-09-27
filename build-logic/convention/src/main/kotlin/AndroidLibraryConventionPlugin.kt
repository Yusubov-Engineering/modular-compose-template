import com.android.build.api.dsl.LibraryExtension
import com.example.modularapp.buildlogic.addUnitTestDependencies
import com.example.modularapp.buildlogic.applyArchitectureGuard
import com.example.modularapp.buildlogic.configureKotlinAndroid
import com.example.modularapp.buildlogic.defaultNamespace
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Every Android library module: SDK levels, Java/Kotlin targets, the
 * architecture guard, detekt, and unit-test dependencies.
 *
 * The namespace is derived from the module path, so a module's build file
 * never has to name it.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("modular.detekt")

        extensions.configure<LibraryExtension> {
            namespace = defaultNamespace()
            configureKotlinAndroid(this)
            defaultConfig.consumerProguardFiles("consumer-rules.pro")
        }

        applyArchitectureGuard()
        addUnitTestDependencies()
    }
}
