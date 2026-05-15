package com.grama.vasathi.data.model

enum class BookingTab {
    PENDING, CONFIRMED, CANCELLED
}

enum class SortOrder {
    NEWEST_FIRST, OLDEST_FIRST, PRICE_HIGH_TO_LOW
}

data class BookingFilter(
    val fromDate: String? = null,
    val toDate: String? = null,
    val sortOrder: SortOrder = SortOrder.NEWEST_FIRST
)

data class ReviewModel(
    val bookingId: String = "",
    val stayId: String = "",
    val guestId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val hygieneRating: Int = 0,
    val timestamp: String = ""
)
