@file:Suppress("unused", "SpellCheckingInspection")

package org.gooseprjkt.bobomb.services.repository

import android.content.Context
import org.gooseprjkt.bobomb.services.core.Service





private var appContext: Context? = null

fun initContext(context: Context) {
    appContext = context.applicationContext
}


val services: List<Service> by lazy {
    appContext?.let {
        DslServicesRepository.loadFromAssets(it, "services.bsl")
    } ?: emptyList()
}


val callServices: List<Service> by lazy {
    appContext?.let {
        DslServicesRepository.loadFromAssets(it, "call_services.bsl")
    } ?: emptyList()
}