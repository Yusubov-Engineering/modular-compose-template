plugins {
    id("modular.android.library")
}

dependencies {
    // api: every call names a DeserializationStrategy, so callers need the type.
    api(libs.kotlinx.serialization.json)
}
