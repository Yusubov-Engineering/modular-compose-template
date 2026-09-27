plugins {
    id("modular.feature.impl")
}

dependencies {
    implementation(project(":features:counter:counter-api"))
    // Counter opens the posts list — through its api, never its impl.
    implementation(project(":features:posts:posts-api"))
}
