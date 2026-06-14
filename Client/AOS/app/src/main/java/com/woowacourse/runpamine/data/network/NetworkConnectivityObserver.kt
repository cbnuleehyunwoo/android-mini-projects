package com.woowacourse.runpamine.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class NetworkConnectivityObserver(
    context: Context,
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isConnected: Boolean
        get() = connectivityManager.hasValidatedInternetConnection()

    val connectionState: Flow<Boolean> =
        callbackFlow {
            val callback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        trySend(connectivityManager.hasValidatedInternetConnection())
                    }

                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities,
                    ) {
                        trySend(networkCapabilities.hasValidatedInternetConnection())
                    }

                    override fun onLost(network: Network) {
                        launch {
                            delay(NETWORK_STATE_SETTLE_MILLIS)
                            trySend(connectivityManager.hasValidatedInternetConnection())
                        }
                    }

                    override fun onUnavailable() {
                        trySend(false)
                    }
                }

            trySend(isConnected)
            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
        }.distinctUntilChanged()
}

private fun ConnectivityManager.hasValidatedInternetConnection(): Boolean {
    val activeNetwork = activeNetwork ?: return false
    val capabilities = getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasValidatedInternetConnection()
}

private fun NetworkCapabilities.hasValidatedInternetConnection(): Boolean =
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

private const val NETWORK_STATE_SETTLE_MILLIS = 200L
