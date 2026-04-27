dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
        // AY -->
        create("aniyomilibs") {
            from(files("../gradle/aniyomi.versions.toml"))
        }
        // <-- AY
    }
}

rootProject.name = "mihon-buildSrc"
