package com.example.zootopia.feature.auth

import kotlinx.coroutines.flow.StateFlow

interface RegisterContract {
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val isSuccess: Boolean = false
    )

    interface Presenter {
        val state: StateFlow<State>
        fun signUp(email: String, pass: String, firstName: String, lastName: String, username: String)
        fun clearError()
    }
}
