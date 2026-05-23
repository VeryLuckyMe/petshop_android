package com.example.zootopia.feature.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zootopia.core.model.Product
import com.example.zootopia.core.model.WishlistItem
import com.example.zootopia.core.utils.CartRepository
import com.example.zootopia.core.utils.NetworkUtils
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductDetailPresenter : ViewModel(), ProductDetailContract.Presenter {

    private val _state = MutableStateFlow(ProductDetailContract.State())
    override val state: StateFlow<ProductDetailContract.State> = _state.asStateFlow()

    override fun loadProduct(productId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch product details
                val fetchedProduct = NetworkUtils.client.postgrest["products"]
                    .select {
                        filter {
                            eq("id", productId)
                        }
                    }
                    .decodeSingle<Product>()

                // Check wishlist status
                var isWish = false
                val user = NetworkUtils.client.auth.currentUserOrNull()
                if (user != null) {
                    val wishlistMatches = NetworkUtils.client.postgrest["wishlist"]
                        .select {
                            filter {
                                eq("user_id", user.id)
                                eq("product_id", productId)
                            }
                        }
                        .decodeList<WishlistItem>()
                    isWish = wishlistMatches.isNotEmpty()
                }

                val enrichedState = getEnrichedState(fetchedProduct).copy(
                    isWishlisted = isWish,
                    isLoading = false
                )
                
                _state.update { enrichedState }
                loadReviews()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load product: ${e.message}"
                    )
                }
            }
        }
    }

    override fun setSize(size: String) {
        _state.update { it.copy(selectedSize = size) }
    }

    override fun setFlavor(flavor: String) {
        _state.update { it.copy(selectedFlavor = flavor) }
    }

    override fun incrementQuantity() {
        _state.update { it.copy(quantity = it.quantity + 1) }
    }

    override fun decrementQuantity() {
        _state.update {
            if (it.quantity > 1) it.copy(quantity = it.quantity - 1) else it
        }
    }

    override fun selectImage(index: Int) {
        _state.update { it.copy(selectedImageIndex = index) }
    }

    override fun setTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    override fun toggleWishlist() {
        val currentState = _state.value
        val product = currentState.product ?: return
        viewModelScope.launch {
            try {
                val user = NetworkUtils.client.auth.currentUserOrNull()
                if (user == null) {
                    _state.update { it.copy(error = "Please log in to manage your wishlist.") }
                    return@launch
                }
                if (currentState.isWishlisted) {
                    NetworkUtils.client.postgrest["wishlist"].delete {
                        filter {
                            eq("user_id", user.id)
                            eq("product_id", product.id ?: 0L)
                        }
                    }
                    _state.update { it.copy(isWishlisted = false, successMessage = "Removed from wishlist") }
                } else {
                    val newItem = WishlistItem(userId = user.id, productId = product.id ?: 0L)
                    NetworkUtils.client.postgrest["wishlist"].insert(newItem)
                    _state.update { it.copy(isWishlisted = true, successMessage = "Added to wishlist") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Wishlist sync failed: ${e.message}") }
            }
        }
    }

    override fun addToCart() {
        val currentState = _state.value
        val product = currentState.product ?: return
        viewModelScope.launch {
            try {
                // Add to repository multiple times matching the quantity
                repeat(currentState.quantity) {
                    CartRepository.addItem(product)
                }
                _state.update { it.copy(successMessage = "Added ${currentState.quantity} item(s) to Cart!") }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to add to cart: ${e.message}") }
            }
        }
    }

    override fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }

    override fun loadReviews() {
        val currentProduct = _state.value.product ?: return
        val productId = currentProduct.id?.toInt() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isReviewsLoading = true, error = null) }
            try {
                val fetchedReviews = NetworkUtils.client.postgrest["reviews"]
                    .select {
                        filter {
                            eq("product_id", productId)
                        }
                    }
                    .decodeList<com.example.zootopia.core.model.Review>()

                val sortedReviews = fetchedReviews.sortedByDescending { it.createdAt }

                val avg = if (sortedReviews.isNotEmpty()) {
                    sortedReviews.map { it.rating }.average().toFloat()
                } else {
                    _state.value.rating
                }

                _state.update {
                    it.copy(
                        reviewsList = sortedReviews,
                        rating = avg,
                        reviewsCount = sortedReviews.size,
                        isReviewsLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isReviewsLoading = false,
                        error = "Failed to load reviews: ${e.message}"
                    )
                }
            }
        }
    }

    override fun setReviewRating(rating: Int) {
        _state.update { it.copy(newReviewRating = rating) }
    }

    override fun setReviewComment(comment: String) {
        _state.update { it.copy(newReviewComment = comment) }
    }

    override fun submitReview() {
        val currentState = _state.value
        val product = currentState.product ?: return
        val commentText = currentState.newReviewComment.trim()
        if (commentText.isEmpty()) {
            _state.update { it.copy(error = "Review comment cannot be empty.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmittingReview = true, error = null) }
            try {
                val user = NetworkUtils.client.auth.currentUserOrNull()
                if (user == null) {
                    _state.update { it.copy(isSubmittingReview = false, error = "Please log in to leave a review.") }
                    return@launch
                }

                var resolvedUsername = user.email?.substringBefore("@") ?: "User"
                try {
                    val profileMatches = NetworkUtils.client.postgrest["zootopiaDatabase"]
                        .select {
                            filter {
                                eq("email", user.email ?: "")
                            }
                        }
                    val profiles = profileMatches.decodeList<com.example.zootopia.core.model.UserProfile>()
                    if (profiles.isNotEmpty()) {
                        val prof = profiles.first()
                        resolvedUsername = if (!prof.firstName.isNullOrEmpty() && !prof.lastName.isNullOrEmpty()) {
                            "${prof.firstName} ${prof.lastName}"
                        } else {
                            prof.username?.ifEmpty { resolvedUsername } ?: resolvedUsername
                        }
                    }
                } catch (e: Exception) {
                    // Silent fallback
                }

                val newReview = com.example.zootopia.core.model.Review(
                    productId = product.id?.toInt() ?: 0,
                    userEmail = user.email ?: "",
                    username = resolvedUsername,
                    rating = currentState.newReviewRating,
                    comment = commentText
                )

                NetworkUtils.client.postgrest["reviews"].insert(newReview)

                _state.update {
                    it.copy(
                        isSubmittingReview = false,
                        newReviewComment = "",
                        newReviewRating = 5,
                        successMessage = "Thank you! Your review was posted successfully."
                    )
                }
                
                loadReviews()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSubmittingReview = false,
                        error = "Failed to submit review: ${e.message}"
                    )
                }
            }
        }
    }

    private fun getEnrichedState(product: Product): ProductDetailContract.State {
        val id = product.id ?: return ProductDetailContract.State(product = product)
        
        val defaultImages = listOf(
            product.imageUrl ?: "",
            "https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=500&q=80",
            "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=500&q=80",
            "https://images.unsplash.com/photo-1534361960057-19889db9621e?w=500&q=80"
        ).filter { it.isNotEmpty() }

        return when (id) {
            1L -> ProductDetailContract.State(
                product = product,
                sku = "PM-10421",
                stockStatus = "In Stock",
                oldPrice = 3125.00,
                discount = "SAVE 20%",
                longDescription = "Royal Canin Adult Premium is a complete and balanced food formulated for medium dogs (11 to 25 kg) from 12 months to 7 years old. It helps support the dog's natural defenses, thanks particularly to an antioxidant complex and manno-oligo-saccharides. Enriched with Omega 3 fatty acids (EPA-DHA) to help maintain healthy skin.",
                features = listOf(
                    "High digestibility for adult dogs",
                    "Omega-3 fatty acids for skin and coat health",
                    "Exclusive kibble design encourages chewing",
                    "Supports natural defenses"
                ),
                sizes = listOf("1 kg", "2 kg", "5 kg", "10 kg", "15 kg"),
                flavors = listOf("Chicken", "Beef", "Salmon", "Lamb"),
                images = defaultImages,
                rating = 4.8f,
                reviewsCount = 42,
                selectedSize = "2 kg",
                selectedFlavor = "Chicken"
            )
            2L -> ProductDetailContract.State(
                product = product,
                sku = "PM-20512",
                stockStatus = "In Stock",
                oldPrice = 2200.00,
                discount = "SAVE 15%",
                longDescription = "Our Professional Grooming Kit is designed to provide a stress-free grooming experience for both you and your pet. The low-noise motor ensures your pet stays calm, while the precision blades provide a clean cut every time.",
                features = listOf(
                    "Low noise design for nervous pets",
                    "High-precision stainless steel blades",
                    "Includes 4 attachment combs",
                    "Long-lasting rechargeable battery"
                ),
                sizes = listOf("Standard"),
                flavors = listOf("Classic Silver"),
                images = defaultImages,
                rating = 4.2f,
                reviewsCount = 28,
                selectedSize = "Standard",
                selectedFlavor = "Classic Silver"
            )
            3L -> ProductDetailContract.State(
                product = product,
                sku = "PM-30119",
                stockStatus = "In Stock",
                oldPrice = 1000.00,
                discount = "SAVE 20%",
                longDescription = "Keep your feline friend active and engaged with our Interactive Feather Cat Toy. The unpredictable movement of the feathers mimics real prey, stimulating your cat's natural hunting instincts.",
                features = listOf(
                    "Durable construction",
                    "Safe, non-toxic materials",
                    "Replaceable feather attachments",
                    "Stimulates mental and physical health"
                ),
                sizes = listOf("One Size"),
                flavors = listOf("Rainbow", "Natural"),
                images = defaultImages,
                rating = 4.5f,
                reviewsCount = 156,
                selectedSize = "One Size",
                selectedFlavor = "Rainbow"
            )
            4L -> ProductDetailContract.State(
                product = product,
                sku = "PM-40223",
                stockStatus = "In Stock",
                oldPrice = 850.00,
                discount = "SAVE 18%",
                longDescription = "Our Organic Bird Seeds are sourced from the finest farms to ensure your feathered friends get the best nutrition possible. No artificial colors or preservatives.",
                features = listOf(
                    "100% Organic ingredients",
                    "Rich in vitamins and minerals",
                    "Supports healthy plumage",
                    "Easy to digest"
                ),
                sizes = listOf("500g", "1kg", "2kg"),
                flavors = listOf("Original Mix"),
                images = defaultImages,
                rating = 4.0f,
                reviewsCount = 15,
                selectedSize = "1kg",
                selectedFlavor = "Original Mix"
            )
            else -> {
                val cat = product.category.lowercase()
                val sizes = when {
                    cat.contains("nutrition") || cat.contains("food") -> listOf("500g", "1kg", "3kg", "5kg")
                    else -> listOf("One Size", "Large")
                }
                val flavors = when {
                    cat.contains("nutrition") || cat.contains("food") -> listOf("Chicken & Rice", "Salmon & Tuna", "Beef Classic")
                    else -> listOf("Original", "Pastel Blue", "Warm Grey")
                }
                val features = listOf(
                    "Premium quality selection",
                    "Ethically sourced & pet safe",
                    "Highly recommended by veterinarians",
                    "Eco-friendly packaging"
                )
                ProductDetailContract.State(
                    product = product,
                    sku = "PM-${10000 + id}",
                    stockStatus = "In Stock",
                    oldPrice = product.price * 1.2,
                    discount = "SAVE 15%",
                    longDescription = "The premium ${product.name} is a curated selection designed to elevate your pet's life. Crafted with carefully-sourced materials/ingredients to provide optimal comfort, health, and happiness.",
                    features = features,
                    sizes = sizes,
                    flavors = flavors,
                    images = defaultImages,
                    rating = 4.6f,
                    reviewsCount = 18,
                    selectedSize = sizes.firstOrNull() ?: "",
                    selectedFlavor = flavors.firstOrNull() ?: ""
                )
            }
        }
    }
}
