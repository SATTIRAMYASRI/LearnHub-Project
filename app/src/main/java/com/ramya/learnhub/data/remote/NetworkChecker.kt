package com.ramya.learnhub.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun interface NetworkChecker {
    fun isOnline(): Boolean
}

class ConnectivityNetworkChecker(context: Context) : NetworkChecker {
    private val manager = context.applicationContext.getSystemService(ConnectivityManager::class.java)

    override fun isOnline(): Boolean {
        val caps = manager?.getNetworkCapabilities(manager.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
