package com.example.zootopia.feature.products

import com.example.zootopia.core.model.Product
import kotlinx.coroutines.flow.StateFlow

interface ProductsContract {
    data class State(
        val products: List<Product> = emptyList(),
        val filteredProducts: List<Product> = emptyList(),
        val categories: List<String> = listOf("All", "Nutrition", "Toys", "Bedding", "Accessories"),
        val selectedCategory: String = "All",
        val searchTerm: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    interface Presenter {
        val state: StateFlow<State>
        fun loadProducts()
        fun setCategory(category: String)
        fun setSearchTerm(term: String)
        fun addToCart(product: Product)
        fun clearError()
    }
}
