package com.grama.vasathi.data.model

data class GuestStats(
    val staysCompleted: Int = 0,
    val savedStays: Int = 0,
    val reviewsGiven: Int = 0
)

data class OfferModel(
    val offerId: String = "",
    val title: String = "",
    val description: String = "",
    val discountPercent: Int = 0,
    val district: String = "",
    val expiryDate: String = "",
    val bannerImageUrl: String = ""
)

data class RecentlyViewedModel(
    val stayId: String = "",
    val stayName: String = "",
    val village: String = "",
    val pricePerNight: Int = 0,
    val thumbnailUrl: String = "",
    val viewedAt: String = ""
)
