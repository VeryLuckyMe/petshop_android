package com.example.zootopia.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zootopia.core.model.UserProfile
import com.example.zootopia.core.utils.NetworkUtils
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterPresenter : ViewModel(), RegisterContract.Presenter {

    private val _state = MutableStateFlow(RegisterContract.State())
    override val state: StateFlow<RegisterContract.State> = _state.asStateFlow()

    override fun signUp(email: String, pass: String, firstName: String, lastName: String, username: String) {
        if (email.isBlank() || pass.isBlank() || firstName.isBlank() || lastName.isBlank() || username.isBlank()) {
            _state.update { it.copy(error = "All fields are required") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                NetworkUtils.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                }

                val profile = UserProfile(
                    username = username,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    role = "user"
                )
                
                NetworkUtils.client.postgrest["zootopiaDatabase"].insert(profile)

                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Signup Failed: ${e.message}") }
            }
        }
    }

    override fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
