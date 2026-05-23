package com.example.zootopia.feature.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zootopia.core.model.Product
import com.example.zootopia.core.utils.CartRepository
import com.example.zootopia.core.utils.NetworkUtils
import io.github.jan.supabase.postgrest.postgrest
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
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Static services matching the premium care theme
                val services = listOf(
                    DashboardContract.ServiceItem("Pet Grooming", "Full-service spa treatments and hygiene care.", Icons.Default.ContentCut),
                    DashboardContract.ServiceItem("Health Checkups", "Routine wellness exams and vaccinations.", Icons.Default.MedicalServices),
                    DashboardContract.ServiceItem("Gourmet Treats", "Organic and nutritionist-approved snacks.", Icons.Default.Restaurant)
                )

                // Query live products from Supabase products catalog
                val fetchedProducts = NetworkUtils.client.postgrest["products"]
                    .select()
                    .decodeList<Product>()
                    .take(4) // Only show top 4 featured products

                _state.update { 
                    it.copy(
                        services = services,
                        products = fetchedProducts,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load dashboard: ${e.message}"
                    ) 
                }
            }
        }
    }

    override fun addToCart(product: Product) {
        CartRepository.addItem(product)
    }
}
