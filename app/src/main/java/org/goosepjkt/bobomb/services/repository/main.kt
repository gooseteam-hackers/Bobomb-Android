@file:Suppress("unused", "SpellCheckingInspection")

package org.goosepjkt.bobomb.services.repository

import android.content.Context
import org.goosepjkt.bobomb.services.core.Service





private var appContext: Context? = null

fun initContext(context: Context) {
    appContext = context.applicationContext
}


val services: List<Service> by lazy {
    appContext?.let {
        DslServicesRepository.loadFromAssets(it, "services.dsl")
    } ?: emptyList()
}


val callServices: List<Service> by lazy {
    appContext?.let {
        DslServicesRepository.loadFromAssets(it, "call_services.dsl")
    } ?: emptyList()
}