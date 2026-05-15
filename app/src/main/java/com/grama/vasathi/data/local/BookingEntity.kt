package com.grama.vasathi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grama.vasathi.data.model.BookingModel

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val bookingId: String,
    val stayId: String,
    val guestId: String,
    val checkIn: String,
    val checkOut: String,
    val guestCount: Int,
    val totalPrice: Int,
    val status: String
)

fun BookingEntity.toModel() = BookingModel(
    bookingId = bookingId,
    stayId = stayId,
    guestId = guestId,
    checkIn = checkIn,
    checkOut = checkOut,
    guestCount = guestCount,
    totalPrice = totalPrice,
    status = status
)

fun BookingModel.toEntity() = BookingEntity(
    bookingId = bookingId,
    stayId = stayId,
    guestId = guestId,
    checkIn = checkIn,
    checkOut = checkOut,
    guestCount = guestCount,
    totalPrice = totalPrice,
    status = status
)
