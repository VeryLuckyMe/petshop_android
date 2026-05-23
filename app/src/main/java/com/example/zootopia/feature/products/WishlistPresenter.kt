package com.example.zootopia.feature.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zootopia.core.model.Product
import com.example.zootopia.core.model.WishlistItem
import com.example.zootopia.core.utils.NetworkUtils
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WishlistPresenter : ViewModel(), WishlistContract.Presenter {

    private val _state = MutableStateFlow(WishlistContract.State())
    override val state: StateFlow<WishlistContract.State> = _state.asStateFlow()

    init {
        loadWishlist()
    }

    override fun loadWishlist() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val user = NetworkUtils.client.auth.currentUserOrNull()
                if (user == null) {
                    _state.update { it.copy(isLoading = false, error = "Please log in to view your wishlist.") }
                    return@launch
                }

                // Query wishlist entries
                val wishlistItems = NetworkUtils.client.postgrest["wishlist"]
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<WishlistItem>()

                if (wishlistItems.isEmpty()) {
                    _state.update { it.copy(wishlistProducts = emptyList(), isLoading = false) }
                    return@launch
                }

                // Fetch matching products
                val productIds = wishlistItems.map { it.productId }
                val allProducts = NetworkUtils.client.postgrest["products"]
                    .select()
                    .decodeList<Product>()

                val filtered = allProducts.filter { productIds.contains(it.id) }

                _state.update {
                    it.copy(
                        wishlistProducts = filtered,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load wishlist: ${e.message}"
                    )
                }
            }
        }
    }

    override fun removeFromWishlist(productId: Long) {
        viewModelScope.launch {
            try {
                val user = NetworkUtils.client.auth.currentUserOrNull()
                if (user == null) {
                    _state.update { it.copy(error = "Please log in to manage your wishlist.") }
                    return@launch
                }

                NetworkUtils.client.postgrest["wishlist"].delete {
                    filter {
                        eq("user_id", user.id)
                        eq("product_id", productId)
                    }
                }

                _state.update { currentState ->
                    val updatedList = currentState.wishlistProducts.filter { it.id != productId }
                    currentState.copy(
                        wishlistProducts = updatedList,
                        successMessage = "Product removed from wishlist."
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to remove item: ${e.message}") }
            }
        }
    }

    override fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun clearSuccess() {
        _state.update { it.copy(successMessage = null) }
    }
}
