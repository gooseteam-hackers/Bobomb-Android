package org.goosepjkt.bobomb.ui

import org.goosepjkt.bobomb.BuildConfig

data class Updates(
    val important: Boolean = false,
    val allowDirect: Boolean = false,
    val description: Map<String, String> = mapOf(),
    val directUrl: String = "",
    val onlyDirect: Boolean = false,
    val telegramUrl: String = "",
    val versionCode: Int = BuildConfig.VERSION_CODE
)
