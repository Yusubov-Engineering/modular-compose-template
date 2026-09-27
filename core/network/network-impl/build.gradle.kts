plugins {
    id("modular.android.library")
    id("modular.di")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    api(project(":core:network:network-api"))
    implementation(project(":core:logger:logger-api"))
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.logging)

    testImplementation(libs.ktor.client.mock)
}
