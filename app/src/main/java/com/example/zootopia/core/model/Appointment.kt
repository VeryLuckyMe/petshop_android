package com.example.zootopia.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Appointment(
    @SerialName("id") val id: Long? = null,
    @SerialName("user_email") val userEmail: String = "",
    @SerialName("service_name") val serviceName: String = "",
    @SerialName("status") val status: String = "pending",
    @SerialName("created_at") val createdAt: String? = null
)
