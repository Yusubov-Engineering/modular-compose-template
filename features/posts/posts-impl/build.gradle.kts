plugins {
    id("modular.feature.impl")
}

dependencies {
    implementation(project(":features:posts:posts-api"))
    implementation(project(":core:network:network-api"))
}
