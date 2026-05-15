package com.grama.vasathi.ui.booking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.grama.vasathi.data.model.*
import com.grama.vasathi.ui.roleselection.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BookingsViewModel(application: Application) : AndroidViewModel(application) {
    private val database by lazy { FirebaseDatabase.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    
    private val _guestBookings = MutableStateFlow<List<BookingModel>>(emptyList())
    val guestBookings = _guestBookings.asStateFlow()

    private val _hostBookings = MutableStateFlow<List<BookingModel>>(emptyList())
    val hostBookings = _hostBookings.asStateFlow()

    private val _selectedTab = MutableStateFlow(BookingTab.PENDING)
    val selectedTab = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private var bookingsListener: ValueEventListener? = null

    init {
        startBookingsListener()
    }

    private fun startBookingsListener() {
        val userId = auth.currentUser?.uid ?: return
        
        bookingsListener = database.getReference("bookings").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allBookings = mutableListOf<BookingModel>()
                for (child in snapshot.children) {
                    child.getValue(BookingModel::class.java)?.let { allBookings.add(it) }
                }
                
                // Filter for Guest
                _guestBookings.value = allBookings.filter { it.guestId == userId }
                
                // Filter for Host (In a real app, you'd match booking.stayId with host's stayIds)
                _hostBookings.value = allBookings 
                
                _isLoading.value = false
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun onTabSelected(tab: BookingTab) {
        _selectedTab.value = tab
    }

    fun acceptBooking(bookingId: String) {
        database.getReference("bookings").child(bookingId).child("status").setValue("CONFIRMED")
    }

    fun rejectBooking(bookingId: String) {
        database.getReference("bookings").child(bookingId).child("status").setValue("CANCELLED")
    }

    fun cancelBooking(bookingId: String) {
        database.getReference("bookings").child(bookingId).child("status").setValue("CANCELLED")
    }

    fun submitReview(bookingId: String, stayId: String, rating: Int, comment: String, hygieneRating: Int) {
        val userId = auth.currentUser?.uid ?: return
        val review = ReviewModel(
            bookingId = bookingId,
            stayId = stayId,
            guestId = userId,
            rating = rating,
            comment = comment,
            hygieneRating = hygieneRating,
            timestamp = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        )
        database.getReference("reviews").child(bookingId).setValue(review)
    }

    override fun onCleared() {
        super.onCleared()
        bookingsListener?.let { database.getReference("bookings").removeEventListener(it) }
    }
}
