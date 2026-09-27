plugins {
    id("modular.android.library")
    id("modular.android.compose")
}

dependencies {
    api(project(":core:router:router-api"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:logger:logger-api"))
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
}
