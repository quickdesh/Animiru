// AM (CONNECTION) -->
package eu.kanade.tachiyomi.data.connection

import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import okhttp3.OkHttpClient

interface Connection {

    val id: Long

    val name: String

    val client: OkHttpClient

    @DrawableRes
    fun getLogo(): Int

    @ColorInt
    fun getLogoColor(): Int

    suspend fun login(username: String, password: String)

    @CallSuper
    fun logout()

    val isLoggedIn: Boolean

    fun getUsername(): String

    fun getPassword(): String

    fun saveCredentials(username: String, password: String)
}
// <-- AM (CONNECTION)
