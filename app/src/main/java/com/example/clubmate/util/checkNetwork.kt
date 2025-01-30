package com.example.clubmate.util


import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


fun isInternetConnected(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // For devices running Android 10 (API level 29) and above
    val network = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
    return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}


fun getInternetConnectionStatus(context: Context): Flow<Boolean> = callbackFlow {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Define a network callback to listen for changes in connectivity
    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(true) // Internet is available
        }

        override fun onLost(network: Network) {
            trySend(false) // Internet is lost
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val hasInternet =
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            trySend(hasInternet) // Emit true or false based on internet availability
        }
    }

    // Register the network callback to listen for network changes
    connectivityManager.registerDefaultNetworkCallback(networkCallback)

    // Clean up the callback when the flow is no longer collected
    awaitClose {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
