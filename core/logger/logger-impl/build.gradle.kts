plugins {
    id("modular.android.library")
    id("modular.di")
}

dependencies {
    api(project(":core:logger:logger-api"))
    implementation(libs.kermit)
}
