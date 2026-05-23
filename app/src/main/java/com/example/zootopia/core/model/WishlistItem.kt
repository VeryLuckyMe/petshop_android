package com.example.zootopia.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WishlistItem(
    @SerialName("id") val id: Long? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("product_id") val productId: Long,
    @SerialName("created_at") val createdAt: String? = null
)
