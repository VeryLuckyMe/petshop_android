package com.example.zootopia.feature.products

import com.example.zootopia.core.model.Product
import kotlinx.coroutines.flow.StateFlow

interface WishlistContract {
    data class State(
        val wishlistProducts: List<Product> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null
    )

    interface Presenter {
        val state: StateFlow<State>
        fun loadWishlist()
        fun removeFromWishlist(productId: Long)
        fun clearError()
        fun clearSuccess()
    }
}
