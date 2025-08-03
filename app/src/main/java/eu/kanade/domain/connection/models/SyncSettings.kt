// AM (SYNC) -->
package eu.kanade.domain.connection.models

data class SyncSettings(
    val animelibEntries: Boolean = true,
    val animeCategories: Boolean = true,
    val episodes: Boolean = true,
    val animeTracking: Boolean = true,
    val animeHistory: Boolean = true,
    val appSettings: Boolean = true,
    val sourceSettings: Boolean = true,
    val privateSettings: Boolean = false,
    // AM (CUSTOM_INFORMATION) -->
    val customInfo: Boolean = true,
    // <-- AM (CUSTOM_INFORMATION)
)
// <-- AM (SYNC)
