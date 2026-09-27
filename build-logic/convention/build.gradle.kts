plugins {
    `kotlin-dsl`
}

group = "com.example.modularapp.buildlogic"

dependencies {
    // compileOnly: build-logic compiles against these plugins but does not
    // ship them. The root build.gradle.kts puts them on the build classpath,
    // with versions, via `apply false` — which is also where Android Studio
    // looks to find the project's AGP version during sync.
    compileOnly(libs.gradle.plugin.android)
    compileOnly(libs.gradle.plugin.kotlin)
    compileOnly(libs.gradle.plugin.compose.compiler)
    compileOnly(libs.gradle.plugin.serialization)
    compileOnly(libs.gradle.plugin.koin.compiler)
    compileOnly(libs.gradle.plugin.detekt)
}

gradlePlugin {
    plugins {
        register("root") {
            id = "modular.root"
            implementationClass = "RootConventionPlugin"
        }
        register("androidApplication") {
            id = "modular.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "modular.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "modular.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("di") {
            id = "modular.di"
            implementationClass = "DependencyInjectionConventionPlugin"
        }
        register("featureApi") {
            id = "modular.feature.api"
            implementationClass = "FeatureApiConventionPlugin"
        }
        register("featureImpl") {
            id = "modular.feature.impl"
            implementationClass = "FeatureImplConventionPlugin"
        }
        register("detekt") {
            id = "modular.detekt"
            implementationClass = "DetektConventionPlugin"
        }
    }
}
