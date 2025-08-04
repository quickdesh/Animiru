// AM (DISCORD_RPC) -->
package eu.kanade.tachiyomi.data.connection.discord

import android.graphics.Color
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.connection.BaseConnection

class Discord(id: Long) : BaseConnection(id, "Discord") {
    override fun getLogo() = R.drawable.ic_discord_24dp

    override fun getLogoColor() = Color.rgb(88, 101, 242)

    override val isLoggedIn: Boolean
        get() = getUsername().isNotEmpty() &&
            getPassword().isNotEmpty()
}
// <-- AM (DISCORD_RPC)
