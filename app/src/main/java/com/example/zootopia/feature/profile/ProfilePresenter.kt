package com.example.zootopia.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zootopia.core.model.UserProfile
import com.example.zootopia.core.utils.NetworkUtils
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfilePresenter : ViewModel(), ProfileContract.Presenter {

    private val _state = MutableStateFlow(ProfileContract.State())
    override val state: StateFlow<ProfileContract.State> = _state.asStateFlow()

    init {
        loadProfile()
    }

    override fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val user = NetworkUtils.client.auth.currentUserOrNull()
                if (user != null) {
                    val fetchedProfile = NetworkUtils.client.postgrest["zootopiaDatabase"]
                        .select {
                            filter {
                                eq("email", user.email ?: "")
                            }
                        }
                        .decodeSingle<UserProfile>()
                    
                    _state.update {
                        it.copy(
                            profile = fetchedProfile,
                            editUsername = fetchedProfile.username ?: "",
                            editFirstName = fetchedProfile.firstName ?: "",
                            editLastName = fetchedProfile.lastName ?: "",
                            isLoading = false
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "User not logged in") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Error fetching profile: ${e.message}") }
            }
        }
    }

    override fun setEditing(isEditing: Boolean) {
        _state.update { it.copy(isEditing = isEditing) }
    }

    override fun updateForm(username: String, firstName: String, lastName: String) {
        _state.update { 
            it.copy(
                editUsername = username,
                editFirstName = firstName,
                editLastName = lastName
            )
        }
    }

    override fun saveProfile() {
        val currentState = _state.value
        val email = currentState.profile?.email ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                NetworkUtils.client.postgrest["zootopiaDatabase"].update({
                    set("username", currentState.editUsername)
                    set("first_name", currentState.editFirstName)
                    set("last_name", currentState.editLastName)
                }) {
                    filter { eq("email", email) }
                }
                
                _state.update { 
                    it.copy(
                        profile = it.profile?.copy(
                            username = it.editUsername, 
                            firstName = it.editFirstName, 
                            lastName = it.editLastName
                        ),
                        isEditing = false,
                        isLoading = false,
                        successMessage = "Profile updated successfully!"
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Save failed: ${e.message}") }
            }
        }
    }

    override fun uploadAvatar(imageBytes: ByteArray) {
        val email = _state.value.profile?.email ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val fileName = "avatar_${System.currentTimeMillis()}.jpg"
                val bucket = NetworkUtils.client.storage.from("avatars")
                
                bucket.upload(fileName, imageBytes, upsert = true)
                val publicUrl = bucket.publicUrl(fileName)
                
                NetworkUtils.client.postgrest["zootopiaDatabase"].update({
                    set("avatar_url", publicUrl)
                }) {
                    filter { eq("email", email) }
                }
                
                _state.update { 
                    it.copy(
                        profile = it.profile?.copy(avatarUrl = publicUrl),
                        isLoading = false,
                        successMessage = "Photo updated successfully!"
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Upload failed: ${e.message}") }
            }
        }
    }

    override fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}
