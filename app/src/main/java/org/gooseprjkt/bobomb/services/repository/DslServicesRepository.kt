@file:Suppress("unused", "SpellCheckingInspection")

package org.gooseprjkt.bobomb.services.repository

import android.content.Context
import org.gooseprjkt.bobomb.services.core.Callback
import org.gooseprjkt.bobomb.services.core.Phone
import org.gooseprjkt.bobomb.services.core.Service
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.InputStreamReader



object DslServicesRepository {

    data class ServiceConfig(
        val id: String,
        val name: String,
        val countryCode: Int,
        val url: String,
        val method: String = "POST",
        val contentType: String = "json",
        val headers: Map<String, String> = emptyMap(),
        val body: String? = null,
        val data: Map<String, String> = emptyMap(),
        val params: Map<String, String> = emptyMap()
    )

    fun loadFromRawString(content: String): List<Service> {
        val services = mutableListOf<Service>()
        val configs = parseDsl(content)

        android.util.Log.d("DslServices", "Parsed ${configs.size} configs from DSL")

        for ((index, config) in configs.withIndex()) {
            try {
                services.add(createServiceFromConfig(config))
            } catch (e: Exception) {
                android.util.Log.e("DslServices", "Error creating service ${index} (${config.id}): ${e.message}", e)
            }
        }

        android.util.Log.d("DslServices", "Created ${services.size} services from DSL")

        return services
    }

    private fun parseDsl(content: String): List<ServiceConfig> {
        val configs = mutableListOf<ServiceConfig>()
        val lines = content.lines()

        var currentConfig: ServiceConfig? = null
        var inServiceBlock = false
        var serviceCount = 0
        var errorCount = 0

        val headers = mutableMapOf<String, String>()
        val data = mutableMapOf<String, String>()
        val params = mutableMapOf<String, String>()
        var body: String? = null

        for ((lineNum, line) in lines.withIndex()) {
            val trimmedLine = line.trim()


            if (trimmedLine.isEmpty() || trimmedLine.startsWith("//")) continue

            when {

                trimmedLine.startsWith("SERVICE ") -> {
                    inServiceBlock = true
                    serviceCount++
                    headers.clear()
                    data.clear()
                    params.clear()
                    body = null

                    val parts = trimmedLine.substringAfter("SERVICE ").split(":")
                    val id = parts.getOrNull(0) ?: ""
                    val name = parts.getOrNull(1) ?: id
                    val countryCode = parts.getOrNull(2)?.toIntOrNull() ?: 7

                    currentConfig = ServiceConfig(
                        id = id,
                        name = name,
                        countryCode = countryCode,
                        url = "",
                        headers = headers.toMap(),
                        data = data.toMap(),
                        params = params.toMap()
                    )
                }


                trimmedLine.startsWith("URL ") && currentConfig != null -> {
                    val url = trimmedLine.substringAfter("URL ").trim()
                    currentConfig = currentConfig!!.copy(url = url)
                }


                trimmedLine.startsWith("METHOD ") && currentConfig != null -> {
                    val method = trimmedLine.substringAfter("METHOD ").trim().uppercase()
                    currentConfig = currentConfig!!.copy(method = method)
                }


                trimmedLine.startsWith("CONTENT_TYPE ") && currentConfig != null -> {
                    val contentType = trimmedLine.substringAfter("CONTENT_TYPE ").trim().lowercase()
                    currentConfig = currentConfig!!.copy(contentType = contentType)
                }


                trimmedLine.startsWith("HEADER ") && currentConfig != null -> {
                    val headerLine = trimmedLine.substringAfter("HEADER ").trim()
                    val parts = headerLine.split(": ", limit = 2)
                    if (parts.size >= 2) {
                        headers[parts[0]] = parts[1]
                        currentConfig = currentConfig!!.copy(headers = headers.toMap())
                    }
                }


                trimmedLine.startsWith("BODY ") && currentConfig != null -> {
                    body = trimmedLine.substringAfter("BODY ").trim()

                    body = body.replace("\\\\", "\\")
                    currentConfig = currentConfig!!.copy(body = body)
                }


                trimmedLine.startsWith("DATA ") && currentConfig != null -> {
                    val dataLine = trimmedLine.substringAfter("DATA ").trim()
                    val parts = dataLine.split("=", limit = 2)
                    if (parts.size == 2) {
                        data[parts[0]] = parts[1]
                        currentConfig = currentConfig!!.copy(data = data.toMap())
                    }
                }


                trimmedLine.startsWith("PARAM ") && currentConfig != null -> {
                    val paramLine = trimmedLine.substringAfter("PARAM ").trim()
                    val parts = paramLine.split("=", limit = 2)
                    if (parts.size == 2) {
                        params[parts[0]] = parts[1]
                        currentConfig = currentConfig!!.copy(params = params.toMap())
                    }
                }


                trimmedLine == "END" && currentConfig != null && inServiceBlock -> {
                    if (currentConfig.url.isNotEmpty()) {
                        configs.add(currentConfig)
                    } else {
                        android.util.Log.w("DslServices", "Service ${currentConfig.id} has no URL at line ~$lineNum")
                        errorCount++
                    }
                    inServiceBlock = false
                    currentConfig = null
                }
            }
        }

        android.util.Log.d("DslServices", "Parsed $serviceCount SERVICE blocks, ${configs.size} valid configs, $errorCount errors")

        return configs
    }

    private fun createServiceFromConfig(config: ServiceConfig): Service {
        return object : Service(config.countryCode) {
            override fun run(client: OkHttpClient, callback: Callback, phone: Phone) {
                val request = buildRequest(config, phone)
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        callback.onError(call, e)
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        response.close()
                        callback.onResponse(call, response)
                    }
                })
            }
        }
    }

    private fun buildRequest(config: ServiceConfig, phone: Phone): Request {
        val builder = Request.Builder()


        var url = replacePlaceholders(config.url, phone)


        config.headers.forEach { (key, value) ->
            builder.addHeader(key, replacePlaceholders(value, phone))
        }


        if (config.params.isNotEmpty()) {
            val httpUrl = url.toHttpUrl().newBuilder()
            config.params.forEach { (key, value) ->
                httpUrl.addQueryParameter(key, replacePlaceholders(value, phone))
            }
            url = httpUrl.build().toString()
        }


        val body = when (config.contentType) {
            "json" -> {
                if (config.body != null) {
                    val jsonBody = replacePlaceholders(config.body, phone)
                    jsonBody.toRequestBody("application/json".toMediaType())
                } else {
                    "".toRequestBody("application/json".toMediaType())
                }
            }
            "form" -> {
                val formBuilder = FormBody.Builder()
                config.data.forEach { (key, value) ->
                    formBuilder.add(key, replacePlaceholders(value, phone))
                }
                formBuilder.build()
            }
            "params" -> {

                "".toRequestBody("application/json".toMediaType())
            }
            else -> {
                "".toRequestBody("application/json".toMediaType())
            }
        }

        builder.url(url)
        builder.method(config.method, body)

        return builder.build()
    }

    private fun replacePlaceholders(value: String, phone: Phone): String {
        var result = value
        result = result.replace("{phone}", phone.phone)
        result = result.replace("{full_phone}", phone.toString())
        result = result.replace("{country_code}", phone.countryCode)
        result = result.replace("{phone_formatted}", phone.phone)
        return result
    }

    fun loadFromAssets(context: Context, assetPath: String = "services.bsl"): List<Service> {
        return try {
            val content = context.assets.open(assetPath).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            }
            android.util.Log.d("DslServices", "Loaded ${content.lines().size} lines from $assetPath")
            val services = loadFromRawString(content)
            android.util.Log.d("DslServices", "Final service count from $assetPath: ${services.size}")


            val byCountry = services.groupBy { it.countryCodes.firstOrNull() ?: 0 }
            byCountry.forEach { (code, list) ->
                android.util.Log.d("DslServices", "  Country $code: ${list.size} services")
            }

            services
        } catch (e: Exception) {
            android.util.Log.e("DslServices", "Error loading $assetPath: ${e.message}", e)
            emptyList()
        }
    }

    fun loadFromResource(resourcePath: String): List<Service> {
        return try {
            val inputStream = DslServicesRepository::class.java.getResourceAsStream(resourcePath)
            if (inputStream != null) {
                val content = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                loadFromRawString(content)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
