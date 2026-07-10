import mihon.gradle.Config

plugins {
    alias(mihonx.plugins.android.library)
    alias(mihonx.plugins.spotless)
    alias(mihonx.plugins.compose)
}

android {
    namespace = "tachiyomi.cast"

    sourceSets {
        getByName("main") {
            if (Config.includeCast) {
                kotlin.directories.add("src/gms/kotlin")
                manifest.srcFile("src/gms/AndroidManifest.xml")
            } else {
                kotlin.directories.add("src/noop/kotlin")
                manifest.srcFile("src/main/kotlin/AndroidManifest.xml")
            }
        }
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.presentationCore)
    implementation(projects.sourceApi)
    implementation(projects.i18nAnimiru)
    implementation(aniyomilibs.mediarouter)
    implementation(libs.androidx.appCompat)

    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.materialIcons)
    implementation(libs.androidx.compose.material3)

    if (Config.includeCast) {
        implementation(aniyomilibs.gms.cast)
    }
}
