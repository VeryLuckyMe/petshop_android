package com.example.zootopia.feature.products

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

class ProductsPresenter : ViewModel(), ProductsContract.Presenter {

    private val _state = MutableStateFlow(ProductsContract.State())
    override val state: StateFlow<ProductsContract.State> = _state.asStateFlow()

    init {
        loadProducts()
    }

    override fun loadProducts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch list of products from Supabase
                val fetchedProducts = NetworkUtils.client.postgrest["products"]
                    .select()
                    .decodeList<Product>()
                
                _state.update { currentState ->
                    currentState.copy(
                        products = fetchedProducts,
                        isLoading = false
                    )
                }
                applyFilters()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load products: ${e.message}"
                    )
                }
            }
        }
    }

    override fun setCategory(category: String) {
        _state.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    override fun setSearchTerm(term: String) {
        _state.update { it.copy(searchTerm = term) }
        applyFilters()
    }

    override fun addToCart(product: Product) {
        CartRepository.addItem(product)
    }

    override fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun applyFilters() {
        val currentState = _state.value
        var list = currentState.products

        // Apply category filter
        if (currentState.selectedCategory != "All") {
            list = list.filter { it.category.equals(currentState.selectedCategory, ignoreCase = true) }
        }

        // Apply text search
        if (currentState.searchTerm.isNotEmpty()) {
            list = list.filter { it.name.contains(currentState.searchTerm, ignoreCase = true) }
        }

        _state.update { it.copy(filteredProducts = list) }
    }
}
