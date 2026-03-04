package org.gooseprjkt.bobomb.services

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonObject

data class ServiceConfigV2(
    val meta: Meta,
    val services: List<ServiceV2>
)

data class Meta(
    val name: String,
    val version: String,
    @SerializedName("total_services")
    val totalServices: Int,
    @SerializedName("test_phone")
    val testPhone: String,
    @SerializedName("success_rate")
    val successRate: String,
    @SerializedName("last_updated")
    val lastUpdated: String,
    val sources: List<String>
)

data class ServiceV2(
    val id: String,
    val name: String,
    val url: String,
    val method: String,
    @SerializedName("content_type")
    val contentType: String,
    val headers: Map<String, String>? = null,
    val body: JsonObject? = null,
    val data: Map<String, String>? = null,
    val params: Map<String, String>? = null,
    val verified: Boolean = false,
    val status: String = "unknown",
    val source: String = ""
)
