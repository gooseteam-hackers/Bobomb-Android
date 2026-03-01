package org.goosepjkt.bobomb.services

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.goosepjkt.bobomb.services.core.Callback
import org.goosepjkt.bobomb.services.core.Phone
import org.goosepjkt.bobomb.services.core.Service
import java.io.IOException

class ServiceParser {
    companion object {
        private const val TAG = "ServiceParser"
        private val gson = Gson()

        fun parseServiceConfigV2(jsonString: String): ServiceConfigV2 {
            return gson.fromJson(jsonString, ServiceConfigV2::class.java)
        }

        fun convertToServicesV2(config: ServiceConfigV2): List<Service> {
            val services = mutableListOf<Service>()

            for (serviceV2 in config.services) {
                try {
                    services.add(createServiceFromServiceV2(serviceV2))
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating service ${serviceV2.id}: ${e.message}", e)
                }
            }

            return services
        }

        private fun createServiceFromServiceV2(serviceV2: ServiceV2): Service {
            return object : Service(7) {
                override fun run(client: OkHttpClient, callback: Callback, phone: Phone) {

                    var formattedUrl = serviceV2.url
                        .replace("{phone}", phone.phone)
                        .replace("{phone_formatted}", phone.format("%1\$s%2\$s%3\$s%4\$s"))
                        .replace("{full_phone}", phone.toString())
                        .replace("{country_code}", phone.countryCode.toString())
                        .replace("*phone*", phone.phone)
                        .replace("*phone2*", phone.phone)


                    var urlWithParams = formattedUrl
                    if (serviceV2.params != null && serviceV2.params.isNotEmpty()) {
                        try {
                            val httpUrl = formattedUrl.toHttpUrlOrNull()
                            if (httpUrl != null) {
                                val httpUrlBuilder = httpUrl.newBuilder()
                                serviceV2.params.forEach { (key, value) ->
                                    val formattedValue = value
                                        .replace("{phone}", phone.phone)
                                        .replace("{phone_formatted}", phone.format("%1\$s%2\$s%3\$s%4\$s"))
                                        .replace("{full_phone}", phone.toString())
                                        .replace("{country_code}", phone.countryCode.toString())
                                    httpUrlBuilder.addQueryParameter(key, formattedValue)
                                }
                                urlWithParams = httpUrlBuilder.build().toString()
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing URL params for ${serviceV2.id}", e)
                        }
                    }

                    val requestBuilder = Request.Builder().url(urlWithParams)


                    serviceV2.headers?.forEach { (key, value) ->
                        val formattedValue = value
                            .replace("{phone}", phone.phone)
                            .replace("{phone_formatted}", phone.format("%1\$s%2\$s%3\$s%4\$s"))
                            .replace("{full_phone}", phone.toString())
                            .replace("{country_code}", phone.countryCode.toString())
                        requestBuilder.addHeader(key, formattedValue)
                    }


                    when (serviceV2.method.uppercase()) {
                        "GET" -> requestBuilder.get()
                        "POST", "PUT", "PATCH" -> {
                            val body = createRequestBodyV2(serviceV2, phone)
                            requestBuilder.method(serviceV2.method.uppercase(), body)
                        }
                        else -> requestBuilder.method(serviceV2.method.uppercase(), null)
                    }

                    client.newCall(requestBuilder.build()).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: Call, e: IOException) {
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

        private fun createRequestBodyV2(serviceV2: ServiceV2, phone: Phone): RequestBody {
            return when (serviceV2.contentType.lowercase()) {
                "json" -> {

                    val jsonBody = serviceV2.body
                    if (jsonBody != null) {
                        val jsonString = gson.toJson(jsonBody)
                            .replace("{phone}", phone.phone)
                            .replace("{phone_formatted}", phone.format("%1\$s%2\$s%3\$s%4\$s"))
                            .replace("{full_phone}", phone.toString())
                            .replace("{country_code}", phone.countryCode.toString())
                        jsonString.toRequestBody("application/json".toMediaType())
                    } else {
                        "".toRequestBody("application/json".toMediaType())
                    }
                }
                "form" -> {

                    val formBuilder = FormBody.Builder()
                    serviceV2.data?.forEach { (key, value) ->
                        val formattedValue = value
                            .replace("{phone}", phone.phone)
                            .replace("{phone_formatted}", phone.format("%1\$s%2\$s%3\$s%4\$s"))
                            .replace("{full_phone}", phone.toString())
                            .replace("{country_code}", phone.countryCode.toString())
                        formBuilder.add(key, formattedValue)
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
        }
    }
}
