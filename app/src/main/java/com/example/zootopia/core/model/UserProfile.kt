package com.example.zootopia.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("id") val id: Long? = null,
    @SerialName("username") val username: String? = "",
    @SerialName("first_name") val firstName: String? = "",
    @SerialName("last_name") val lastName: String? = "",
    @SerialName("email") val email: String? = "",
    @SerialName("role") val role: String? = "user",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
