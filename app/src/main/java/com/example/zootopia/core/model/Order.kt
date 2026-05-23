package com.example.zootopia.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    @SerialName("id") val id: Long? = null,
    @SerialName("name") val name: String,
    @SerialName("price") val price: Double,
    @SerialName("quantity") val quantity: Int,
    @SerialName("image_url") val imageUrl: String? = null
)

@Serializable
data class Order(
    @SerialName("id") val id: String? = null,
    @SerialName("user_email") val userEmail: String,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("status") val status: String = "pending",
    @SerialName("shipping_address") val shippingAddress: String,
    @SerialName("items") val items: List<OrderItem>
)
