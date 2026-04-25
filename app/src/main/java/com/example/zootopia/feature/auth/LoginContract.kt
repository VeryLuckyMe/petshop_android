package com.example.zootopia.feature.auth

import kotlinx.coroutines.flow.StateFlow

interface LoginContract {
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val isSuccess: Boolean = false
    )

    interface Presenter {
        val state: StateFlow<State>
        fun login(email: String, pass: String)
        fun clearError()
    }
}
