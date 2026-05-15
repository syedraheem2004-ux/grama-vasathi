package com.grama.vasathi.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooking(booking: BookingEntity)

    @Query("SELECT * FROM bookings WHERE bookingId = :id")
    fun getBookingById(id: String): Flow<BookingEntity?>
}
