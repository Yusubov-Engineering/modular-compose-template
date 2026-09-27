package com.example.modularapp.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { IllegalArgumentException("No library '$alias' in libs.versions.toml") }

internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias).orElseThrow { IllegalArgumentException("No version '$alias' in libs.versions.toml") }
        .requiredVersion

/** The Android + Kotlin settings every module shares, application or library. */
internal fun Project.configureKotlinAndroid(android: CommonExtension) {
    android.apply {
        compileSdk = libs.version("android-compileSdk").toInt()
        defaultConfig.minSdk = libs.version("android-minSdk").toInt()
        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
        testOptions.unitTests.isReturnDefaultValues = true
    }
    // AGP 9 compiles Kotlin itself ("built-in Kotlin"), so there is no
    // org.jetbrains.kotlin.android plugin to apply — only its extension.
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.version("jvmTarget")))
        }
    }
}

/** Unit-test dependencies every module gets, so no module has to ask. */
internal fun Project.addUnitTestDependencies() {
    dependencies.apply {
        add("testImplementation", libs.lib("junit"))
        add("testImplementation", libs.lib("kotlinx-coroutines-test"))
        add("testImplementation", libs.lib("turbine"))
    }
}

/**
 * `:features:posts:posts-impl` -> `com.example.modularapp.features.posts.impl`,
 * `:features:user-profile:user-profile-api` -> `…features.userprofile.api`.
 * Keep each module's Kotlin package equal to this, so its `R` class needs no import.
 */
internal fun Project.defaultNamespace(): String {
    val segments = path.removePrefix(":").split(':')
    val parent = segments.dropLast(1)
    val leaf = segments.last().substringAfterLast('-')
    return (listOf(BASE_PACKAGE) + parent + leaf)
        .joinToString(".") { it.replace("-", "") }
}

internal const val BASE_PACKAGE = "com.example.modularapp"
