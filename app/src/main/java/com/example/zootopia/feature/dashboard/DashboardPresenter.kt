package com.example.zootopia.feature.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardPresenter : ViewModel(), DashboardContract.Presenter {

    private val _state = MutableStateFlow(DashboardContract.State())
    override val state: StateFlow<DashboardContract.State> = _state.asStateFlow()

    init {
        loadData()
    }

    override fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Simulate network delay or just load dummy data
            val services = listOf(
                DashboardContract.ServiceItem("Pet Grooming", "Full-service spa treatments and hygiene care.", Icons.Default.ContentCut),
                DashboardContract.ServiceItem("Health Checkups", "Routine wellness exams and vaccinations.", Icons.Default.MedicalServices),
                DashboardContract.ServiceItem("Gourmet Treats", "Organic and nutritionist-approved snacks.", Icons.Default.Restaurant)
            )

            val products = listOf(
                DashboardContract.ProductItem("Organic Chicken Bites", "$14.99", "Nutrition", true),
                DashboardContract.ProductItem("Rubber Bone", "$9.50", "Toys"),
                DashboardContract.ProductItem("Dream Cloud Bed", "$45.00", "Bedding"),
                DashboardContract.ProductItem("Water Fountain", "$29.99", "Accessories")
            )

            _state.update { 
                it.copy(
                    services = services,
                    products = products,
                    isLoading = false
                ) 
            }
        }
    }
}
