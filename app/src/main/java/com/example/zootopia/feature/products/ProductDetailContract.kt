package com.example.zootopia.feature.products

import com.example.zootopia.core.model.Product
import com.example.zootopia.core.model.Review
import kotlinx.coroutines.flow.StateFlow

interface ProductDetailContract {
    data class State(
        val product: Product? = null,
        val sku: String = "PM-10001",
        val stockStatus: String = "In Stock",
        val oldPrice: Double = 0.0,
        val discount: String = "",
        val longDescription: String = "",
        val features: List<String> = emptyList(),
        val sizes: List<String> = listOf("Standard"),
        val flavors: List<String> = listOf("Original"),
        val images: List<String> = emptyList(),
        val rating: Float = 4.5f,
        val reviewsCount: Int = 24,
        val selectedSize: String = "",
        val selectedFlavor: String = "",
        val quantity: Int = 1,
        val selectedImageIndex: Int = 0,
        val selectedTab: Int = 0, // 0 = Description, 1 = Specs, 2 = Reviews
        val isWishlisted: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,
        
        // Reviews dynamic updates
        val reviewsList: List<Review> = emptyList(),
        val isReviewsLoading: Boolean = false,
        val isSubmittingReview: Boolean = false,
        val newReviewRating: Int = 5,
        val newReviewComment: String = "",
        val isLoggedIn: Boolean = false
    )

    interface Presenter {
        val state: StateFlow<State>
        fun loadProduct(productId: Long)
        fun setSize(size: String)
        fun setFlavor(flavor: String)
        fun incrementQuantity()
        fun decrementQuantity()
        fun selectImage(index: Int)
        fun setTab(index: Int)
        fun toggleWishlist()
        fun addToCart()
        fun clearError()
        fun clearSuccessMessage()
        
        // Reviews dynamic interactions
        fun loadReviews()
        fun setReviewRating(rating: Int)
        fun setReviewComment(comment: String)
        fun submitReview()
    }
}

