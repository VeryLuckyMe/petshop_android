package com.example.zootopia.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zootopia.core.model.Appointment
import com.example.zootopia.core.model.UserProfile
import com.example.zootopia.core.model.Pet
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
                    loadAppointments()
                    loadPets()
                } else {
                    _state.update { it.copy(isLoading = false, error = "User not logged in") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Error fetching profile: ${e.message}") }
            }
        }
    }

    override fun setTab(tabIndex: Int) {
        _state.update { it.copy(selectedTab = tabIndex) }
        if (tabIndex == 1) {
            loadAppointments()
        } else if (tabIndex == 4) {
            loadPets()
        }
    }

    override fun loadAppointments() {
        val email = _state.value.profile?.email ?: return
        viewModelScope.launch {
            try {
                val fetchedAppointments = NetworkUtils.client.postgrest["appointments"]
                    .select {
                        filter {
                            eq("user_email", email)
                        }
                    }
                    .decodeList<Appointment>()
                
                _state.update { it.copy(appointments = fetchedAppointments) }
            } catch (e: Exception) {
                // Non-fatal, just set error
                _state.update { it.copy(error = "Failed to load appointments: ${e.message}") }
            }
        }
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
                        successMessage = "Profile updated successfully!",
                        selectedTab = 0 // Switch back to View tab
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Save failed: ${e.message}") }
            }
        }
    }

    override fun updatePasswordForm(newPass: String, confirmPass: String) {
        _state.update {
            it.copy(
                newPassword = newPass,
                confirmPassword = confirmPass
            )
        }
    }

    override fun changePassword() {
        val currentState = _state.value
        val newPass = currentState.newPassword
        val confirmPass = currentState.confirmPassword

        if (newPass.isBlank() || confirmPass.isBlank()) {
            _state.update { it.copy(error = "Password fields cannot be empty.") }
            return
        }

        if (newPass != confirmPass) {
            _state.update { it.copy(error = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                NetworkUtils.client.auth.updateUser {
                    password = newPass
                }
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        newPassword = "",
                        confirmPassword = "",
                        successMessage = "Password changed successfully!",
                        selectedTab = 0 // Switch back to Profile View
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Failed to update password: ${e.message}") }
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

    override fun loadPets() {
        val user = NetworkUtils.client.auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            try {
                val fetchedPets = NetworkUtils.client.postgrest["pets"]
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<Pet>()
                
                _state.update { it.copy(pets = fetchedPets, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Failed to load pets: ${e.message}") }
            }
        }
    }

    override fun addPet(name: String, type: String, breed: String, age: String, special: String) {
        val user = NetworkUtils.client.auth.currentUserOrNull() ?: return
        if (name.isBlank()) {
            _state.update { it.copy(error = "Pet name cannot be blank") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val newPet = Pet(
                    userId = user.id,
                    name = name,
                    type = type,
                    breed = breed.ifBlank { null },
                    age = age.ifBlank { null },
                    specialInstructions = special.ifBlank { null }
                )
                NetworkUtils.client.postgrest["pets"].insert(newPet)
                _state.update { it.copy(successMessage = "Pet registered successfully!") }
                loadPets()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Failed to add pet: ${e.message}") }
            }
        }
    }

    override fun deletePet(petId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                NetworkUtils.client.postgrest["pets"].delete {
                    filter {
                        eq("id", petId)
                    }
                }
                _state.update { it.copy(successMessage = "Pet profile removed!") }
                loadPets()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Failed to delete pet: ${e.message}") }
            }
        }
    }

    override fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}
