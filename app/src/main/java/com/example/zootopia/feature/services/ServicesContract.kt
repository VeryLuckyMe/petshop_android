package com.example.zootopia.feature.services

import com.example.zootopia.core.model.Pet
import com.example.zootopia.core.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface ServicesContract {
    data class Service(
        val name: String,
        val description: String,
        val price: Double,
        val duration: String,
        val category: String
    )

    data class Addon(
        val id: String,
        val name: String,
        val price: Double
    )

    data class State(
        val services: List<Service> = emptyList(),
        val selectedCategory: String = "All",
        val categories: List<String> = listOf("All", "Wellness", "Clinical", "Spa"),
        val isLoading: Boolean = false,
        val successMessage: String? = null,
        val error: String? = null,
        val profile: UserProfile? = null,
        val pets: List<Pet> = emptyList()
    )

    interface Presenter {
        val state: StateFlow<State>
        fun loadServices()
        fun loadProfileAndPets()
        fun setCategory(category: String)
        fun bookAppointment(
            serviceName: String,
            addons: List<Addon>,
            date: String,
            time: String,
            petName: String,
            paymentMethod: String,
            redeemedPoints: Int
        )
        fun clearMessages()
    }
}
