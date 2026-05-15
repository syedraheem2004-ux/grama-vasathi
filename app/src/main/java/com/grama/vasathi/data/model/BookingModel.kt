package com.grama.vasathi.data.model

data class BookingModel(
    val bookingId: String = "",
    val stayId: String = "",
    val guestId: String = "",
    val checkIn: String = "",
    val checkOut: String = "",
    val guestCount: Int = 1,
    val totalPrice: Int = 0,
    val status: String = "PENDING" // "PENDING", "ACCEPTED", "REJECTED"
)
