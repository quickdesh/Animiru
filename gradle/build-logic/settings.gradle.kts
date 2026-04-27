dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../libs.versions.toml"))
        }
        create("mihonx") {
            from(files("../mihon.versions.toml"))
        }
        // AM -->
        create("aniyomilibs") {
            from(files("../aniyomi.versions.toml"))
        }
        // <-- AM
    }
}

rootProject.name = "build-logic"
