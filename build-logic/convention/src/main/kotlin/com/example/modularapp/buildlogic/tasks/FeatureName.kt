package com.example.modularapp.buildlogic.tasks

import org.gradle.api.GradleException

/** One feature name, in each spelling the files need. `user-profile` is the input. */
internal data class FeatureName(val kebab: String) {
    init {
        if (!kebab.matches(Regex("[a-z][a-z0-9]*(-[a-z0-9]+)*"))) {
            throw GradleException("Feature name must be kebab-case, like 'profile' or 'user-profile': '$kebab'.")
        }
    }

    /** `userprofile` — a package segment, matching the module namespace build-logic derives. */
    val packageSegment: String = kebab.replace("-", "")

    /** `UserProfile` */
    val pascal: String = kebab.split('-').joinToString("") { part -> part.replaceFirstChar(Char::uppercaseChar) }

    /** `userProfile` */
    val camel: String = pascal.replaceFirstChar(Char::lowercaseChar)

    /** `user_profile`, for resource names. */
    val snake: String = kebab.replace('-', '_')
}
