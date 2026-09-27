plugins {
    id("modular.android.library")
    id("modular.android.compose")
}

dependencies {
    // api: AppRoute extends NavKey, and ModuleRouter's functions take
    // Navigation 3 and kotlinx.serialization scopes as receivers.
    api(libs.androidx.navigation3.runtime)
    api(libs.kotlinx.serialization.json)
}
