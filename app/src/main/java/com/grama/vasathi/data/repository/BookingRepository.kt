package com.grama.vasathi.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.grama.vasathi.data.local.BookingDao
import com.grama.vasathi.data.local.toEntity
import com.grama.vasathi.data.model.BookingModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BookingRepository(private val bookingDao: BookingDao) {
    private val database = FirebaseDatabase.getInstance().getReference("bookings")

    suspend fun submitBooking(booking: BookingModel) = withContext(Dispatchers.IO) {
        try {
            // Write to Firebase
            database.child(booking.bookingId).setValue(booking).await()
            
            // Write to Local Room
            bookingDao.insertBooking(booking.toEntity())
            
            true
        } catch (e: Exception) {
            false
        }
    }
}
