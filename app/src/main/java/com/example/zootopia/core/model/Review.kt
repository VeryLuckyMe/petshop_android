package com.example.zootopia.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Review(
    @SerialName("id") val id: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("product_id") val productId: Int,
    @SerialName("user_email") val userEmail: String,
    @SerialName("username") val username: String,
    @SerialName("rating") val rating: Int,
    @SerialName("comment") val comment: String
)
