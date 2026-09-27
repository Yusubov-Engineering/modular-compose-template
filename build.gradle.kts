// Every plugin the build uses, declared once with its version and not applied
// here. Modules apply them through the convention plugins in build-logic/
// (`id("modular.feature.impl")` and friends), never by raw id.
//
// This block is load-bearing: build-logic only compiles against these plugins
// (compileOnly), so this is what puts them on the build's classpath — and it
// is where Android Studio reads the AGP version from during sync. Remove it
// and sync fails with "Unable to determine project AGP version".
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.koin.compiler) apply false
    alias(libs.plugins.detekt) apply false
    id("modular.root")
}
