package mihon.gradle

import org.gradle.api.Project

interface BuildConfig {
    // AM -->
    val includeCast: Boolean
    // <-- AM
    val enableUpdater: Boolean
    val enableCodeShrink: Boolean
    val includeDependencyInfo: Boolean
}

val Project.Config: BuildConfig get() = object : BuildConfig {
    // AM -->
    override val includeCast: Boolean = project.hasProperty("include-cast")
    // <-- AM
    override val enableUpdater: Boolean = project.hasProperty("enable-updater")
    override val enableCodeShrink: Boolean = !project.hasProperty("disable-code-shrink")
    override val includeDependencyInfo: Boolean = project.hasProperty("include-dependency-info")
}
