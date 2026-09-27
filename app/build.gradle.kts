plugins {
    id("modular.android.application")
}

android {
    namespace = "com.example.modularapp"

    defaultConfig {
        applicationId = "com.example.modularapp"
        versionCode = 1
        versionName = "1.0.0"
    }
}

// The app is the only module allowed to depend on -impl modules: it is the
// composition root. The architecture guard in build-logic fails the build if
// any other module tries.
dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:router:router-impl"))
    implementation(project(":core:logger:logger-impl"))
    implementation(project(":core:network:network-impl"))

    implementation(project(":features:counter:counter-api"))
    implementation(project(":features:counter:counter-impl"))
    implementation(project(":features:posts:posts-api"))
    implementation(project(":features:posts:posts-impl"))
    // <generated:feature-dependencies>

    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)
}
