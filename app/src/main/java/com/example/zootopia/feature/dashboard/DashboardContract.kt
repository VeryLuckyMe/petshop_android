package com.example.zootopia.feature.dashboard

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.StateFlow

interface DashboardContract {
    data class ServiceItem(val title: String, val description: String, val icon: ImageVector)
    data class ProductItem(val name: String, val price: String, val category: String, val isHot: Boolean = false)

    data class State(
        val services: List<ServiceItem> = emptyList(),
        val products: List<ProductItem> = emptyList(),
        val isLoading: Boolean = false
    )

    interface Presenter {
        val state: StateFlow<State>
        fun loadData()
    }
}
