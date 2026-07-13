plugins {
    alias(mihonx.plugins.android.library)
    alias(mihonx.plugins.spotless)

    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "tachiyomi.domain"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}

dependencies {
    implementation(projects.sourceApi)
    implementation(projects.core.common)
    // AM (GROUPING) -->
    implementation(projects.i18n)
    implementation(projects.i18nAniyomi)
    implementation(projects.i18nAnimiru)
    // <-- AM (GROUPING)

    implementation(libs.bundles.kotlinx.coroutines)
    implementation(libs.bundles.serialization)

    implementation(libs.unifile)

    api(libs.sqldelight.androidxPaging)

    compileOnly(platform(libs.androidx.compose.bom))
    compileOnly(libs.androidx.compose.runtimeAnnotation)
    // AM (CAST) -->
    implementation(libs.androidx.compose.foundation)
    // <-- AM (CAST)

    testImplementation(libs.bundles.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}
