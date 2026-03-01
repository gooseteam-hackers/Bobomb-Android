package org.goosepjkt.bobomb.services.repository

import com.google.gson.JsonObject
import org.goosepjkt.bobomb.services.DefaultFormatter.format
import org.goosepjkt.bobomb.services.core.Callback
import org.goosepjkt.bobomb.services.core.Phone
import org.goosepjkt.bobomb.services.core.Service
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

data class RemoteService(
    val phoneCodes: List<Int>,
    val requests: List<RemoteRequest>
)

data class RemoteRequest(
    val method: String,
    val url: String,
    val json: JsonObject? = null,
    val data: HashMap<String, String>? = null,
    val headers: HashMap<String, String>? = null
)

private fun processRemoteService(
    phoneCodes: IntArray,
    request: RemoteRequest
): Service {
    return object : Service(*phoneCodes) {
        override fun run(
            client: OkHttpClient,
            callback: Callback,
            phone: Phone
        ) {
            val body: RequestBody? = when {
                request.json != null -> {
                    format(
                        request.json.toString(),
                        phone
                    ).toRequestBody("application/json".toMediaType())
                }

                request.data != null -> {
                    FormBody.Builder().apply {
                        request.data.forEach { entry ->
                            add(entry.key, format(entry.value, phone))
                        }
                    }.build()
                }

                else -> null
            }

            val headersBuilder = Headers.Builder()

            if (request.headers != null)
                request.headers.map { entry ->
                    headersBuilder.addUnsafeNonAscii(entry.key, format(entry.value, phone))
                }

            client.newCall(
                Request.Builder()
                    .url(format(request.url, phone))
                    .headers(headersBuilder.build())
                    .method(request.method, body)
                    .build()
            )
                .enqueue(callback)
        }
    }
}

fun collect(
    client: OkHttpClient,
    url: String
): MutableList<Service> = try {
    val response = client.newCall(
        Request.Builder()
            .url(url)
            .get()
            .build()
    ).execute()

    val remoteServices = response.body?.string()?.let {
        com.google.gson.Gson().fromJson(it, Array<RemoteService>::class.java)
    } ?: emptyArray()

    mutableListOf<Service>().apply {
        for (remoteService in remoteServices) {
            for (request in remoteService.requests) {
                add(
                    processRemoteService(
                        remoteService.phoneCodes.toIntArray(),
                        request
                    )
                )
            }
        }
    }
} catch (e: Exception) {
    e.printStackTrace()
    mutableListOf()
}
