package com.example.zootopia.feature.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zootopia.core.model.Appointment
import com.example.zootopia.core.model.Pet
import com.example.zootopia.core.model.UserProfile
import com.example.zootopia.core.utils.NetworkUtils
import com.example.zootopia.core.utils.SessionManager
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ServicesPresenter : ViewModel(), ServicesContract.Presenter {

    private val _state = MutableStateFlow(ServicesContract.State())
    override val state: StateFlow<ServicesContract.State> = _state.asStateFlow()

    init {
        loadServices()
        loadProfileAndPets()
    }

    override fun loadServices() {
        val staticServices = listOf(
            ServicesContract.Service("General Consultation", "Full wellness exam, diagnostic checks, and care planning.", 45.00, "30 mins", "Wellness"),
            ServicesContract.Service("Complete Grooming Spa", "Deep organic wash, styling blow dry, nail clipping, and ear cleaning.", 55.00, "60 mins", "Spa"),
            ServicesContract.Service("Core Vaccinations", "Rabies, DHPP, and booster combinations with immunization records.", 30.00, "15 mins", "Wellness"),
            ServicesContract.Service("Ultrasonic Dental Scaling", "Plaque removal, tartar scaling, polish, and dental hygiene plan.", 85.00, "45 mins", "Clinical"),
            ServicesContract.Service("Diagnostic Blood Panel", "Comprehensive metabolic and complete blood count (CBC) screenings.", 110.00, "30 mins", "Clinical"),
            ServicesContract.Service("Medicated Flea Bath", "Soothing therapeutic bath to treat sensitive skin, bugs, and parasites.", 40.00, "40 mins", "Spa")
        )
        _state.update { it.copy(services = staticServices) }
    }

    override fun loadProfileAndPets() {
        viewModelScope.launch {
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

                    val fetchedPets = NetworkUtils.client.postgrest["pets"]
                        .select {
                            filter {
                                eq("user_id", user.id)
                            }
                        }
                        .decodeList<Pet>()

                    _state.update {
                        it.copy(
                            profile = fetchedProfile,
                            pets = fetchedPets
                        )
                    }
                }
            } catch (e: Exception) {
                // Non-fatal fallback
            }
        }
    }

    override fun setCategory(category: String) {
        _state.update { it.copy(selectedCategory = category) }
    }

    override fun bookAppointment(
        serviceName: String,
        addons: List<ServicesContract.Addon>,
        date: String,
        time: String,
        petName: String,
        paymentMethod: String,
        redeemedPoints: Int
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val email = SessionManager.getCurrentUserEmail()
                if (email.isNullOrEmpty()) {
                    _state.update { it.copy(isLoading = false, error = "Please log in to book an appointment.") }
                    return@launch
                }

                // Compile formatted appointment service description matching the web logic
                var compiledName = "$serviceName on $date at $time for $petName"
                if (addons.isNotEmpty()) {
                    val addonNames = addons.joinToString { it.name }
                    compiledName += " with Add-ons: ($addonNames)"
                }

                if (paymentMethod == "loyalty_points" && redeemedPoints > 0) {
                    val discount = (redeemedPoints / 10) * 50
                    compiledName += " (Paid with points. Discount: ₱$discount)"

                    // Deduct loyalty points from profile
                    val profile = _state.value.profile
                    if (profile != null) {
                        val newPoints = maxOf(0, profile.loyaltyPoints - redeemedPoints)
                        NetworkUtils.client.postgrest["zootopiaDatabase"].update({
                            set("loyalty_points", newPoints)
                        }) {
                            filter { eq("email", email) }
                        }
                    }
                }

                val newAppointment = Appointment(
                    userEmail = email,
                    serviceName = compiledName,
                    status = "pending"
                )

                // Write appointment record to Supabase
                NetworkUtils.client.postgrest["appointments"]
                    .insert(newAppointment)

                _state.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "Appointment for '$serviceName' booked successfully!"
                    )
                }

                // Reload profile to refresh points
                loadProfileAndPets()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to book appointment: ${e.message}"
                    )
                }
            }
        }
    }

    override fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}
