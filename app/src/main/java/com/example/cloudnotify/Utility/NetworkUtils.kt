package com.example.cloudnotify.Utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NetworkUtils(private  val context:Context) {

    fun observeNetworkState():Flow<Boolean> =
    callbackFlow {
        val connctivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network:Network) {
                trySend(true)

            }
            override fun onLost(network:Network){
                Log.d("NetworkUtils", "Network is lost")

                trySend(false)

            }

        }
        val networkRequest=NetworkRequest.Builder().build()
        connctivityManager.registerNetworkCallback(networkRequest,networkCallback)
        awaitClose {
            connctivityManager.unregisterNetworkCallback(networkCallback)
        }


    }
}