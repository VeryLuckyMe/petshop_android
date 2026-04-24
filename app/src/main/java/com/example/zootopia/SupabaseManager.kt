package com.example.zootopia

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

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

object SupabaseManager {
    val client = createSupabaseClient(
        supabaseUrl = "https://nmrotznuvmdlfcraogku.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5tcm90em51dm1kbGZjcmFvZ2t1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzM0MDEyMjksImV4cCI6MjA4ODk3NzIyOX0.WPKfUgqeBijHc5CvxXeQbfDq6_eSA6BKxN986yYv2EQ"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}
