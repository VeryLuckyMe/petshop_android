package com.example.zootopia.core.utils

import com.example.zootopia.core.model.CartItem
import com.example.zootopia.core.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object CartRepository {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun addItem(product: Product) {
        _items.update { currentList ->
            val existingIndex = currentList.indexOfFirst { it.product.id == product.id }
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
    }

    fun updateQuantity(productId: Long, quantity: Int) {
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
    }

    fun removeItem(productId: Long) {
        _items.update { currentList ->
            currentList.filter { it.product.id != productId }
        }
    }

    fun clearCart() {
        _items.value = emptyList()
    }

    fun getCartTotal(): Double {
        return _items.value.sumOf { it.product.price * it.quantity }
    }

    fun getCartItemsCount(): Int {
        return _items.value.sumOf { it.quantity }
    }
}
