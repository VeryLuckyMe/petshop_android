package com.example.zootopia.feature.dashboard

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.zootopia.core.model.Product
import kotlinx.coroutines.flow.StateFlow

interface DashboardContract {
    data class ServiceItem(val title: String, val description: String, val icon: ImageVector)

    data class State(
        val services: List<ServiceItem> = emptyList(),
        val products: List<Product> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    interface Presenter {
        val state: StateFlow<State>
        fun loadData()
        fun addToCart(product: Product)
    }
}
