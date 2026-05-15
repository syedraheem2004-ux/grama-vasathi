package com.grama.vasathi.data.model

data class ExploreFilter(
    val minPrice: Int = 500,
    val maxPrice: Int = 5000,
    val minRating: Float = 0f,
    val district: String = "All",
    val activities: List<String> = emptyList(),
    val bathroomType: String = "Any",
    val guestCount: Int = 1,
    val checkIn: String? = null,
    val checkOut: String? = null,
    val sortOrder: ExploreSortOrder = ExploreSortOrder.NEWEST_FIRST
)

enum class ViewMode { MAP, GRID }
enum class ExploreTab { EXPLORE, WISHLIST }
enum class ExploreSortOrder {
    NEWEST_FIRST, HIGHEST_RATED,
    PRICE_LOW_TO_HIGH, PRICE_HIGH_TO_LOW,
    NEAREST
}
