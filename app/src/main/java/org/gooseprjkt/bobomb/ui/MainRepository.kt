package org.gooseprjkt.bobomb.ui

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import org.gooseprjkt.bobomb.BuildVars
import org.gooseprjkt.bobomb.BuildVars.AttackSpeed
import org.gooseprjkt.bobomb.BuildVars.DripModeType
import org.gooseprjkt.bobomb.services.core.Service
import org.gooseprjkt.bobomb.services.repository.DslServicesRepository
import org.gooseprjkt.bobomb.worker.AuthProxy
import okhttp3.Credentials.basic
import okhttp3.OkHttpClient
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy

class MainRepository(private val context: Context?) {
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context!!)
    private val recentPrefs: SharedPreferences = context!!.getSharedPreferences("recent_numbers", Context.MODE_PRIVATE)

    var theme: Int
        get() = preferences.getInt(THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(mode) {
            preferences.edit().putInt(THEME, mode).apply()
        }

    var lastPhone: String?
        get() = preferences.getString(LAST_PHONE, "")
        set(phoneNumber) {
            preferences.edit().putString(LAST_PHONE, phoneNumber).apply()
        }

    var lastCountryCode: Int
        get() = preferences.getInt(LAST_COUNTRY_CODE, 0)
        set(phoneCode) {
            preferences.edit().putInt(LAST_COUNTRY_CODE, phoneCode).apply()
        }

    var rawProxy: String?
        get() = preferences.getString(PROXY, "")
        set(proxyStrings) {
            preferences.edit().putString(PROXY, proxyStrings).apply()
        }

    val proxy: List<AuthProxy>
        get() = parseProxy(rawProxy)

    fun parseProxy(proxyStrings: String?): List<AuthProxy> {
        if (proxyStrings!!.isEmpty()) return ArrayList()

        require(!proxyStrings.startsWith("\n"))

        return mutableListOf<AuthProxy>().apply {
            proxyStrings.split("\n")
                .filter { it.isNotBlank() }
                .forEach { proxy ->
                    var proxyString = proxy.trim()
                    var credential: String? = null

                    if (proxyString.contains(" ")) {
                        val data = proxyString.split(" ", limit = 2)
                        proxyString = data[0].trim()
                        if (data.size > 1 && data[1].contains(":")) {
                            val loginData = data[1].split(":", limit = 2)
                            credential = basic(loginData[0].trim(), loginData[1].trim())
                        }
                    }

                    val parts = proxyString.split(":", limit = 2)
                    if (parts.size >= 2) {
                        try {
                            add(
                                AuthProxy(
                                    Proxy.Type.HTTP,
                                    InetSocketAddress.createUnresolved(parts[0].trim(), parts[1].trim().toInt()),
                                    credential
                                )
                            )
                        } catch (e: NumberFormatException) {
                            // Invalid port, skip this proxy
                        }
                    }
                }
        }
    }

    fun addRecentNumber(phone: String) {
        val recentNumbers = getRecentNumbers().toMutableList()
        val fullNumber = "+$phone"
        
        recentNumbers.remove(fullNumber)
        recentNumbers.add(0, fullNumber)
        
        if (recentNumbers.size > MAX_RECENT_NUMBERS) {
            recentNumbers.removeAt(recentNumbers.size - 1)
        }
        
        recentPrefs.edit()
            .putInt("recent_count", recentNumbers.size)
            .apply()
        
        recentNumbers.forEachIndexed { index, number ->
            recentPrefs.edit()
                .putString("recent_$index", number)
                .apply()
        }
    }

    fun getRecentNumbers(): List<String> {
        val count = recentPrefs.getInt("recent_count", 0)
        return mutableListOf<String>().apply {
            for (i in 0 until count) {
                recentPrefs.getString("recent_$i", null)?.let { add(it) }
            }
        }
    }

    fun removeRecentNumber(fullNumber: String) {
        val recentNumbers = getRecentNumbers().toMutableList()
        recentNumbers.remove(fullNumber)

        // Обновляем SharedPreferences
        recentPrefs.edit()
            .putInt("recent_count", recentNumbers.size)
            .apply()

        // Очищаем все старые записи
        for (i in 0 until MAX_RECENT_NUMBERS) {
            recentPrefs.edit().remove("recent_$i").apply()
        }

        // Записываем обновлённый список
        recentNumbers.forEachIndexed { index, number ->
            recentPrefs.edit()
                .putString("recent_$index", number)
                .apply()
        }
    }

    fun clearRecentNumbers() {
        val count = recentPrefs.getInt("recent_count", 0)
        val editor = recentPrefs.edit().clear()
        for (i in 0 until count) {
            editor.remove("recent_$i")
        }
        editor.putInt("recent_count", 0).apply()
    }

    var isProxyEnabled: Boolean
        get() = preferences.getBoolean(PROXY_ENABLED, false)
        set(enabled) {
            preferences.edit().putBoolean(PROXY_ENABLED, enabled).apply()
        }

    var isSnowfallEnabled: Boolean
        get() = preferences.getBoolean(SNOWFALL_ENABLED, false)
        set(enabled) {
            preferences.edit().putBoolean(SNOWFALL_ENABLED, enabled).apply()
        }

    var remoteServicesUrls: Set<String>?
        get() = preferences.getStringSet(REMOTE_SERVICES_URL, HashSet())
        set(urls) {
            preferences.edit().putStringSet(REMOTE_SERVICES_URL, urls).apply()
        }

    var isRemoteServicesEnabled: Boolean
        get() = preferences.getBoolean(REMOTE_SERVICES, false)
        set(enabled) {
            preferences.edit().putBoolean(REMOTE_SERVICES, enabled).apply()
        }

    var isCallCentersEnabled: Boolean
        get() = preferences.getBoolean(CALL_CENTERS, true)
        set(enabled) {
            preferences.edit().putBoolean(CALL_CENTERS, enabled).apply()
        }

    var isCallsEnabled: Boolean
        get() = preferences.getBoolean(ALL_CALLS_ENABLED, true)
        set(enabled) {
            preferences.edit().putBoolean(ALL_CALLS_ENABLED, enabled).apply()
        }

    var isMessagesEnabled: Boolean
        get() = preferences.getBoolean(ALL_MESSAGES_ENABLED, true)
        set(enabled) {
            preferences.edit().putBoolean(ALL_MESSAGES_ENABLED, enabled).apply()
        }

    fun getAllRepositories(client: OkHttpClient): List<() -> List<Service>> = buildList {
        android.util.Log.d("MainRepository", "getAllRepositories called")

        add {
            val services = DslServicesRepository.loadFromAssets(context!!, "services.bsl")
            android.util.Log.d("MainRepository", "Loaded ${services.size} services from BSL")
            services
        }
    }


    fun getTotalServicesCount(countryCode: String): Int {
        val allServices = DslServicesRepository.loadFromAssets(context!!, "services.bsl")
        return allServices.count { service ->
            service.countryCodes.any { it == countryCode.toIntOrNull() ?: 7 }
        }
    }

    var attackSpeed: AttackSpeed
        get() = AttackSpeed.entries[preferences.getInt(ATTACK_SPEED, AttackSpeed.SLOW.ordinal)]
        set(attackSpeed) {
            preferences.edit().putInt(ATTACK_SPEED, attackSpeed.ordinal).apply()
        }

    var isDripModeEnabled: Boolean
        get() = preferences.getBoolean(DRIP_MODE_ENABLED, BuildVars.DRIP_MODE_ENABLED_BY_DEFAULT)
        set(enabled) {
            preferences.edit().putBoolean(DRIP_MODE_ENABLED, enabled).apply()
        }

    var dripDelayMs: Long
        get() = preferences.getLong(DRIP_MODE_DELAY_MS, BuildVars.DRIP_MODE_DELAY_MS)
        set(delay) {
            preferences.edit().putLong(DRIP_MODE_DELAY_MS, delay).apply()
        }

    var dripModeType: DripModeType
        get() = DripModeType.entries[preferences.getInt(DRIP_MODE_TYPE, DripModeType.SINGLE_SESSION.ordinal)]
        set(type) {
            preferences.edit().putInt(DRIP_MODE_TYPE, type.ordinal).apply()
        }

    var isDripRandomDelayEnabled: Boolean
        get() = preferences.getBoolean(DRIP_MODE_RANDOM_DELAY_ENABLED, BuildVars.DRIP_MODE_RANDOM_DELAY_ENABLED_BY_DEFAULT)
        set(enabled) {
            preferences.edit().putBoolean(DRIP_MODE_RANDOM_DELAY_ENABLED, enabled).apply()
        }

    var dripRandomDelayMinMs: Long
        get() = preferences.getLong(DRIP_MODE_RANDOM_DELAY_MIN_MS, BuildVars.DRIP_MODE_RANDOM_DELAY_MIN_MINUTES * 60 * 1000L)
        set(delay) {
            preferences.edit().putLong(DRIP_MODE_RANDOM_DELAY_MIN_MS, delay).apply()
        }

    var dripRandomDelayMaxMs: Long
        get() = preferences.getLong(DRIP_MODE_RANDOM_DELAY_MAX_MS, BuildVars.DRIP_MODE_RANDOM_DELAY_MAX_MINUTES * 60 * 1000L)
        set(delay) {
            preferences.edit().putLong(DRIP_MODE_RANDOM_DELAY_MAX_MS, delay).apply()
        }

    companion object {
        private const val THEME = "theme"
        private const val LAST_PHONE = "last_phone"
        private const val LAST_COUNTRY_CODE = "last_country_code"
        private const val PROXY = "proxy"
        private const val PROXY_ENABLED = "proxy_enabled"
        private const val SNOWFALL_ENABLED = "snowfall_enabled"
        private const val ATTACK_SPEED = "attack_speed"
        private const val REMOTE_SERVICES = "remote_services"
        private const val CALL_CENTERS = "call_centers"
        private const val REMOTE_SERVICES_URL = "remote_services_url"
        private const val ALL_CALLS_ENABLED = "all_calls_enabled"
        private const val ALL_MESSAGES_ENABLED = "all_messages_enabled"
        
        // Drip Mode keys
        private const val DRIP_MODE_ENABLED = "drip_mode_enabled"
        private const val DRIP_MODE_DELAY_MS = "drip_mode_delay_ms"
        private const val DRIP_MODE_TYPE = "drip_mode_type"
        private const val DRIP_MODE_RANDOM_DELAY_ENABLED = "drip_mode_random_delay_enabled"
        private const val DRIP_MODE_RANDOM_DELAY_MIN_MS = "drip_mode_random_delay_min_ms"
        private const val DRIP_MODE_RANDOM_DELAY_MAX_MS = "drip_mode_random_delay_max_ms"

        private const val MAX_RECENT_NUMBERS = 10
    }
}
