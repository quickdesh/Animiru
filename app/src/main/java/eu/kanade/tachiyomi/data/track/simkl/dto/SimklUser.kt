// AY -->
package eu.kanade.tachiyomi.data.track.simkl.dto

import kotlinx.serialization.Serializable

@Serializable
data class SimklUser(
    val user: SimklUserData,
    val account: SimklUserAccount,
)

@Serializable
data class SimklUserData(
    val name: String,
)

@Serializable
data class SimklUserAccount(
    val id: Int,
)
// <-- AY
