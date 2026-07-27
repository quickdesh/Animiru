package eu.kanade.tachiyomi.util.system

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.getSystemService
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.Inet4Address
import kotlin.coroutines.resume

val Context.connectivityManager: ConnectivityManager
    get() = getSystemService()!!

val Context.wifiManager: WifiManager
    get() = getSystemService()!!

fun Context.isOnline(): Boolean {
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    val maxTransport = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> NetworkCapabilities.TRANSPORT_LOWPAN
        else -> NetworkCapabilities.TRANSPORT_WIFI_AWARE
    }
    return (NetworkCapabilities.TRANSPORT_CELLULAR..maxTransport).any(networkCapabilities::hasTransport)
}

/**
 * Returns true if device is connected to Wifi.
 */
fun Context.isConnectedToWifi(): Boolean {
    if (!wifiManager.isWifiEnabled) return false

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        @Suppress("DEPRECATION")
        wifiManager.connectionInfo.bssid != null
    }
}

// AM (CAST) -->
suspend fun Context.getWanIp(): String? {
    return suspendCancellableCoroutine { continuation ->
        val cm = connectivityManager

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                val ip = linkProperties.linkAddresses
                    .map { it.address }
                    .filterIsInstance<Inet4Address>()
                    .firstOrNull { !it.isLoopbackAddress }
                    ?.hostAddress

                if (ip != null && continuation.isActive) {
                    continuation.resume(ip)
                    cm.unregisterNetworkCallback(this)
                }
            }

            override fun onUnavailable() {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        }

        cm.registerNetworkCallback(request, callback)
        continuation.invokeOnCancellation {
            cm.unregisterNetworkCallback(callback)
        }
    }
}
// <-- AM (CAST)
