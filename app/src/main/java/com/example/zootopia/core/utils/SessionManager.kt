package com.example.zootopia.core.utils

import com.example.zootopia.core.model.UserProfile
import io.github.jan.supabase.gotrue.auth

object SessionManager {
    var currentUserProfile: UserProfile? = null

    suspend fun getCurrentUserEmail(): String? {
        return NetworkUtils.client.auth.currentUserOrNull()?.email
    }
}
