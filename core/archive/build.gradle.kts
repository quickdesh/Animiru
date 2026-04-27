plugins {
    alias(mihonx.plugins.android.library)
    alias(mihonx.plugins.spotless)

    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "mihon.core.archive"
}

dependencies {
    implementation(libs.unifile)
}
