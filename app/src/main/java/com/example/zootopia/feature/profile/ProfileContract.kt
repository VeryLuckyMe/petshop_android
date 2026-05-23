package com.example.zootopia.feature.profile

import com.example.zootopia.core.model.Appointment
import com.example.zootopia.core.model.UserProfile
import com.example.zootopia.core.model.Pet
import kotlinx.coroutines.flow.StateFlow

interface ProfileContract {
    data class State(
        val profile: UserProfile? = null,
        val appointments: List<Appointment> = emptyList(),
        val pets: List<Pet> = emptyList(),
        val isLoading: Boolean = true,
        val isEditing: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,
        val selectedTab: Int = 0, // 0 = Profile, 1 = My Appointments, 2 = Edit Profile, 3 = Change Password, 4 = My Pets
        
        // Edit Profile states
        val editUsername: String = "",
        val editFirstName: String = "",
        val editLastName: String = "",

        // Change Password states
        val newPassword: String = "",
        val confirmPassword: String = ""
    )

    interface Presenter {
        val state: StateFlow<State>
        fun loadProfile()
        fun setTab(tabIndex: Int)
        fun loadAppointments()
        fun loadPets()
        fun addPet(name: String, type: String, breed: String, age: String, special: String)
        fun deletePet(petId: Long)
        fun updateForm(username: String, firstName: String, lastName: String)
        fun saveProfile()
        fun updatePasswordForm(newPass: String, confirmPass: String)
        fun changePassword()
        fun uploadAvatar(imageBytes: ByteArray)
        fun clearMessages()
    }
}

