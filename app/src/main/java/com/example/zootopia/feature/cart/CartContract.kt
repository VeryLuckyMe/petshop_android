package com.example.zootopia.feature.cart

import com.example.zootopia.core.model.CartItem
import kotlinx.coroutines.flow.StateFlow

interface CartContract {
    data class State(
        val items: List<CartItem> = emptyList(),
        val subtotal: Double = 0.0,
        val shipping: Double = 100.0,
        val loyaltyPoints: Int = 0,
        val redeemedPoints: Int = 0,
        val discountAmount: Double = 0.0,
        val total: Double = 0.0,
        val isCheckingOut: Boolean = false,
        val checkoutSuccess: Boolean = false,
        val error: String? = null
    )

    interface Presenter {
        val state: StateFlow<State>
        fun updateQuantity(productId: Long, quantity: Int)
        fun removeItem(productId: Long)
        fun clearCart()
        fun startCheckout()
        fun dismissSuccess()
        fun clearError()
        fun redeemPoints(points: Int)
    }
}
