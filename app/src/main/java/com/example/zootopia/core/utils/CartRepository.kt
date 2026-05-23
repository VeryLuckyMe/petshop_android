package com.example.zootopia.core.utils

import com.example.zootopia.core.model.CartItem
import com.example.zootopia.core.model.Product
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseCartRow(
    @SerialName("user_email") val userEmail: String,
    @SerialName("product_id") val productId: Long,
    @SerialName("quantity") val quantity: Int
)

object CartRepository {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()
    private val scope = CoroutineScope(Dispatchers.IO)

    // Loads and synchronizes memory state flow with direct Supabase 'cart' table
    suspend fun syncCartWithServer() {
        val user = NetworkUtils.client.auth.currentUserOrNull() ?: return
        val email = user.email ?: return
        try {
            // 1. Fetch all products to resolve items references
            val allProducts = NetworkUtils.client.postgrest["products"]
                .select()
                .decodeList<Product>()
            
            // 2. Fetch active cart rows from server
            val rows = NetworkUtils.client.postgrest["cart"]
                .select {
                    filter {
                        eq("user_email", email)
                    }
                }
                .decodeList<SupabaseCartRow>()
            
            // 3. Map database rows to CartItem structures
            val syncedItems = rows.mapNotNull { row ->
                val matchingProd = allProducts.find { it.id == row.productId }
                if (matchingProd != null) {
                    CartItem(matchingProd, row.quantity)
                } else {
                    null
                }
            }

            _items.value = syncedItems
        } catch (e: Exception) {
            // Fallback gracefully to local memory cache on network failures
        }
    }

    fun addItem(product: Product) {
        val productId = product.id ?: return
        
        // 1. Update local UI memory state flow immediately for responsive feedback
        _items.update { currentList ->
            val existingIndex = currentList.indexOfFirst { it.product.id == productId }
            if (existingIndex != -1) {
                currentList.mapIndexed { index, cartItem ->
                    if (index == existingIndex) {
                        cartItem.copy(quantity = cartItem.quantity + 1)
                    } else {
                        cartItem
                    }
                }
            } else {
                currentList + CartItem(product, 1)
            }
        }

        // 2. Dispatch async write to Supabase backend in the background
        scope.launch {
            val user = NetworkUtils.client.auth.currentUserOrNull() ?: return@launch
            val email = user.email ?: return@launch
            try {
                val existing = NetworkUtils.client.postgrest["cart"]
                    .select {
                        filter {
                            eq("user_email", email)
                            eq("product_id", productId)
                        }
                    }
                    .decodeList<SupabaseCartRow>()

                if (existing.isNotEmpty()) {
                    NetworkUtils.client.postgrest["cart"].update({
                        set("quantity", existing[0].quantity + 1)
                    }) {
                        filter {
                            eq("user_email", email)
                            eq("product_id", productId)
                        }
                    }
                } else {
                    val newRow = SupabaseCartRow(email, productId, 1)
                    NetworkUtils.client.postgrest["cart"].insert(newRow)
                }
            } catch (e: Exception) {
                // Fail silently in background to preserve offline-first flow
            }
        }
    }

    fun updateQuantity(productId: Long, quantity: Int) {
        // 1. Update local UI memory state flow immediately
        _items.update { currentList ->
            if (quantity <= 0) {
                currentList.filter { it.product.id != productId }
            } else {
                currentList.map { cartItem ->
                    if (cartItem.product.id == productId) {
                        cartItem.copy(quantity = quantity)
                    } else {
                        cartItem
                    }
                }
            }
        }

        // 2. Dispatch async update/delete to Supabase backend
        scope.launch {
            val user = NetworkUtils.client.auth.currentUserOrNull() ?: return@launch
            val email = user.email ?: return@launch
            try {
                if (quantity <= 0) {
                    NetworkUtils.client.postgrest["cart"].delete {
                        filter {
                            eq("user_email", email)
                            eq("product_id", productId)
                        }
                    }
                } else {
                    NetworkUtils.client.postgrest["cart"].update({
                        set("quantity", quantity)
                    }) {
                        filter {
                            eq("user_email", email)
                            eq("product_id", productId)
                        }
                    }
                }
            } catch (e: Exception) { }
        }
    }

    fun removeItem(productId: Long) {
        // 1. Update local UI memory state flow immediately
        _items.update { currentList ->
            currentList.filter { it.product.id != productId }
        }

        // 2. Dispatch async delete to Supabase backend
        scope.launch {
            val user = NetworkUtils.client.auth.currentUserOrNull() ?: return@launch
            val email = user.email ?: return@launch
            try {
                NetworkUtils.client.postgrest["cart"].delete {
                    filter {
                        eq("user_email", email)
                        eq("product_id", productId)
                    }
                }
            } catch (e: Exception) { }
        }
    }

    fun clearCart() {
        // 1. Update local UI memory state flow immediately
        _items.value = emptyList()

        // 2. Dispatch async purge to Supabase backend
        scope.launch {
            val user = NetworkUtils.client.auth.currentUserOrNull() ?: return@launch
            val email = user.email ?: return@launch
            try {
                NetworkUtils.client.postgrest["cart"].delete {
                    filter {
                        eq("user_email", email)
                    }
                }
            } catch (e: Exception) { }
        }
    }

    fun getCartTotal(): Double {
        return _items.value.sumOf { it.product.price * it.quantity }
    }

    fun getCartItemsCount(): Int {
        return _items.value.sumOf { it.quantity }
    }
}
