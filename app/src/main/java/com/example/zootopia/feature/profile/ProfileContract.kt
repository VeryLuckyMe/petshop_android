package com.example.zootopia.feature.profile

import android.net.Uri
import com.example.zootopia.core.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface ProfileContract {
    data class State(
        val profile: UserProfile? = null,
        val isLoading: Boolean = true,
        val isEditing: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,
        
        // Form states
        val editUsername: String = "",
        val editFirstName: String = "",
        val editLastName: String = ""
    )

    interface Presenter {
        val state: StateFlow<State>
        fun loadProfile()
        fun setEditing(isEditing: Boolean)
        fun updateForm(username: String, firstName: String, lastName: String)
        fun saveProfile()
        fun uploadAvatar(imageBytes: ByteArray)
        fun clearMessages()
    }
}
