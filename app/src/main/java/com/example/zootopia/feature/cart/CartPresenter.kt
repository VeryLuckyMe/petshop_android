package com.example.zootopia.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zootopia.core.utils.CartRepository
import com.example.zootopia.core.utils.NetworkUtils
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CartPresenter : ViewModel(), CartContract.Presenter {

    private val _state = MutableStateFlow(CartContract.State())
    override val state: StateFlow<CartContract.State> = _state.asStateFlow()

    init {
        loadLoyaltyPoints()
        
        viewModelScope.launch {
            CartRepository.items.collect { currentItems ->
                val subtotal = currentItems.sumOf { it.product.price * it.quantity }
                // Free shipping if subtotal > ₱1000, otherwise ₱100
                val shipping = if (subtotal > 1000.0 || subtotal == 0.0) 0.0 else 100.0

                _state.update {
                    val total = Math.max(0.0, subtotal + shipping - it.discountAmount)
                    it.copy(
                        items = currentItems,
                        subtotal = subtotal,
                        shipping = shipping,
                        total = total
                    )
                }
            }
        }
    }

    private fun loadLoyaltyPoints() {
        viewModelScope.launch {
            try {
                val user = NetworkUtils.client.auth.currentUserOrNull()
                if (user != null) {
                    val fetchedProfile = NetworkUtils.client.postgrest["zootopiaDatabase"]
                        .select {
                            filter {
                                eq("email", user.email ?: "")
                            }
                        }
                        .decodeSingle<com.example.zootopia.core.model.UserProfile>()
                    
                    _state.update { currentState ->
                        currentState.copy(
                            loyaltyPoints = fetchedProfile.loyaltyPoints,
                            redeemedPoints = 0,
                            discountAmount = 0.0
                        )
                    }
                }
            } catch (e: Exception) {
                // Non-fatal, keep 0 points
            }
        }
    }

    override fun updateQuantity(productId: Long, quantity: Int) {
        CartRepository.updateQuantity(productId, quantity)
    }

    override fun removeItem(productId: Long) {
        CartRepository.removeItem(productId)
    }

    override fun clearCart() {
        CartRepository.clearCart()
    }

    override fun startCheckout() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.items.isEmpty()) {
                _state.update { it.copy(error = "Your cart is empty!") }
                return@launch
            }
            _state.update { it.copy(isCheckingOut = true) }
            
            try {
                val user = NetworkUtils.client.auth.currentUserOrNull()
                if (user == null) {
                    _state.update { it.copy(isCheckingOut = false, error = "Please log in to checkout.") }
                    return@launch
                }

                // 1. Fetch user's default shipping address from 'addresses' table
                var addressStr = "No default address saved. Contact Customer Support."
                try {
                    val addrList = NetworkUtils.client.postgrest["addresses"]
                        .select {
                            filter {
                                eq("user_id", user.id)
                                eq("is_default", true)
                            }
                        }
                        .decodeList<com.example.zootopia.core.model.Address>()
                    
                    if (addrList.isNotEmpty()) {
                        val addrData = addrList[0]
                        addressStr = "${addrData.fullName} (${addrData.phone}) | ${addrData.addressLine1}${
                            if (!addrData.addressLine2.isNullOrEmpty()) ", " + addrData.addressLine2 else ""
                        }, ${addrData.city}, ${addrData.stateProvince} ${addrData.postalCode} [${addrData.label ?: "Home"}]"
                    }
                } catch (addrEx: Exception) {
                    // Non-fatal, use fallback string
                }

                // 2. Prepare Order items and payload
                val orderItemsList = currentState.items.map {
                    com.example.zootopia.core.model.OrderItem(
                        id = it.product.id,
                        name = it.product.name,
                        price = it.product.price,
                        quantity = it.quantity,
                        imageUrl = it.product.imageUrl
                    )
                }

                val orderPayload = com.example.zootopia.core.model.Order(
                    userEmail = user.email ?: "",
                    totalAmount = currentState.total,
                    status = "pending",
                    shippingAddress = addressStr,
                    items = orderItemsList
                )

                // 3. Write order record to Supabase
                NetworkUtils.client.postgrest["orders"].insert(orderPayload)

                // 4. Update loyalty points if redeemed
                if (currentState.redeemedPoints > 0) {
                    val newPoints = Math.max(0, currentState.loyaltyPoints - currentState.redeemedPoints)
                    NetworkUtils.client.postgrest["zootopiaDatabase"].update({
                        set("loyalty_points", newPoints)
                    }) {
                        filter { eq("email", user.email ?: "") }
                    }
                }
                
                // Simulate payment processing delay
                kotlinx.coroutines.delay(1500)
                
                CartRepository.clearCart()
                _state.update { 
                    it.copy(
                        isCheckingOut = false,
                        checkoutSuccess = true,
                        redeemedPoints = 0,
                        discountAmount = 0.0,
                        loyaltyPoints = Math.max(0, it.loyaltyPoints - currentState.redeemedPoints)
                    ) 
                }
            } catch (e: Exception) {
                _state.update { it.copy(isCheckingOut = false, error = "Checkout failed: ${e.message}") }
            }
        }
    }

    override fun redeemPoints(points: Int) {
        val capped = Math.min(points, _state.value.loyaltyPoints)
        val rounded = (capped / 10) * 10
        val discount = (rounded / 10) * 50.0

        _state.update {
            val total = Math.max(0.0, it.subtotal + it.shipping - discount)
            it.copy(
                redeemedPoints = rounded,
                discountAmount = discount,
                total = total
            )
        }
    }

    override fun dismissSuccess() {
        _state.update { it.copy(checkoutSuccess = false) }
    }

    override fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
