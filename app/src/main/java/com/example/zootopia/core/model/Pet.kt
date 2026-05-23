package com.example.zootopia.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pet(
    @SerialName("id") val id: Long? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String = "dog",
    @SerialName("breed") val breed: String? = null,
    @SerialName("age") val age: String? = null,
    @SerialName("special_instructions") val specialInstructions: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
