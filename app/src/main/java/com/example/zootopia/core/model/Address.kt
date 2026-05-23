package com.example.zootopia.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Address(
    @SerialName("id") val id: Long? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("full_name") val fullName: String,
    @SerialName("phone") val phone: String,
    @SerialName("address_line_1") val addressLine1: String,
    @SerialName("address_line_2") val addressLine2: String? = null,
    @SerialName("city") val city: String,
    @SerialName("state_province") val stateProvince: String,
    @SerialName("postal_code") val postalCode: String,
    @SerialName("country") val country: String = "Philippines",
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("label") val label: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
