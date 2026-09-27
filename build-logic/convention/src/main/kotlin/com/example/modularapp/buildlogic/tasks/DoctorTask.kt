package com.example.modularapp.buildlogic.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * The rules that compile fine and fail at runtime:
 *
 * - **feature-not-registered**: a feature's Koin module is missing from the
 *   app's DependencyInjectionConfiguration. Everything compiles; its routes
 *   are never collected and navigating to it crashes.
 * - **feature-not-a-dependency**: the app does not depend on a feature's -impl.
 * - **route-not-registered**: a route is declared but missing from its
 *   ModuleRouter's `registerRoutes()`. It navigates fine, and crashes the
 *   first time the back stack is saved.
 *
 * Dependency-direction rules are not here: build-logic's architecture guard
 * enforces those on every build.
 */
@DisableCachingByDefault(because = "Reads source files directly and always reports.")
abstract class DoctorTask : DefaultTask() {
    @get:Internal
    abstract val rootDirectory: DirectoryProperty

    @TaskAction
    fun check() {
        val root = rootDirectory.get().asFile
        val appBuild = root.resolve("app/build.gradle.kts").readText()
        val diConfiguration = root.walkKotlin("app/src/main")
            .first { it.name == "DependencyInjectionConfiguration.kt" }
            .readText()

        val problems = featureImplModules(root).flatMap { module ->
            val feature = FeatureName(module.parentFile.name)
            val gradlePath = ":" + module.relativeTo(root).invariantSeparatorsPath.replace('/', ':')
            buildList {
                if ("${feature.pascal}KoinModule::class" !in diConfiguration) {
                    add("feature-not-registered: ${feature.pascal}KoinModule is not in the app's includes.")
                }
                if ("project(\"$gradlePath\")" !in appBuild) {
                    add("feature-not-a-dependency: app/build.gradle.kts does not depend on $gradlePath.")
                }
                addAll(unregisteredRoutes(root, module))
            }
        }

        if (problems.isNotEmpty()) {
            throw GradleException(problems.joinToString("\n", prefix = "doctor found problems:\n") { "  ✖ $it" })
        }
        logger.lifecycle("doctor: every feature is registered and every route is saveable.")
    }

    private fun featureImplModules(root: File): List<File> =
        root.resolve("features").listFiles().orEmpty()
            .flatMap { it.listFiles().orEmpty().toList() }
            .filter { it.name.endsWith("-impl") && it.resolve("build.gradle.kts").isFile }
            .sortedBy { it.path }

    private fun unregisteredRoutes(root: File, module: File): List<String> {
        val sources = root.walkKotlin(module.resolve("src/main").relativeTo(root).path).map { it.readText() }.toList()
        val routes = sources.flatMap { source ->
            ROUTE_DECLARATION.findAll(source).map { it.groupValues[1] }.toList()
        }
        val registrations = sources.joinToString("\n")
        return routes
            .filter { "subclass($it::class)" !in registrations }
            .map { "route-not-registered: $it (${module.name}) is missing from registerRoutes()." }
    }

    private fun File.walkKotlin(relative: String): Sequence<File> =
        resolve(relative).walkTopDown().filter { it.isFile && it.extension == "kt" }

    private companion object {
        val ROUTE_DECLARATION = Regex("""@Serializable\s+(?:internal\s+)?data\s+(?:object|class)\s+(\w+)[^{\n]*:\s*AppRoute""")
    }
}
