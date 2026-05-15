package com.grama.vasathi.data.model

data class StayModel(
    val id: String = "",
    val hostName: String = "",
    val village: String = "",
    val district: String = "",
    val activities: List<String> = emptyList(),
    val hygieneScore: Float = 0f,
    val pricePerNight: Int = 0,
    val imageUrl: String = "",
    val rating: Float = 0f
)
