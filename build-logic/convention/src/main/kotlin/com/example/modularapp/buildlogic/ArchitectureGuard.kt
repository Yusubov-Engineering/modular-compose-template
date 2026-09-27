package com.example.modularapp.buildlogic

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

/**
 * Enforces the dependency rules at configuration time, so breaking one fails
 * the build instead of waiting for review:
 *
 * - only `:app` may depend on an `-impl` module;
 * - nothing may depend on `:app`.
 *
 * Everything else follows from these two: a feature can reach another only
 * through its `-api`, and an `-api` cannot reach an implementation at all.
 */
internal fun Project.applyArchitectureGuard() {
    val self = path
    configurations.configureEach {
        val configurationName = name
        dependencies.withType(ProjectDependency::class.java).configureEach {
            val target = path
            val violation = when {
                target == self -> null // a module's own test classpaths
                target == ":app" ->
                    "nothing may depend on :app"
                target.endsWith("-impl") && self != ":app" ->
                    "only :app may depend on an -impl module; depend on its -api instead"
                else -> null
            }
            if (violation != null) {
                throw GradleException(
                    "Architecture rule broken in $self ($configurationName -> $target): $violation.",
                )
            }
        }
    }
}
