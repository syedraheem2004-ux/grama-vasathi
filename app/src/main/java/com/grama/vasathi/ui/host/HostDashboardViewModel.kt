package com.grama.vasathi.ui.host

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.grama.vasathi.data.model.BookingModel
import com.grama.vasathi.data.model.EarningStats
import com.grama.vasathi.data.model.StayModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HostDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance() }
    
    private val _bookingRequests = MutableStateFlow<List<BookingModel>>(emptyList())
    val bookingRequests = _bookingRequests.asStateFlow()

    private val _earnings = MutableStateFlow(EarningStats(24, 3, 14400, listOf(3000, 4500, 2900, 4000)))
    val earnings = _earnings.asStateFlow()

    private val _checklistProgress = MutableStateFlow(7) // 7 out of 10 items
    val checklistProgress = _checklistProgress.asStateFlow()

    private val _listings = MutableStateFlow<List<StayModel>>(emptyList())
    val listings = _listings.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private var bookingsListener: ValueEventListener? = null
    private var staysListener: ValueEventListener? = null

    init {
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        val currentUserId = auth.currentUser?.uid ?: return
        
        // Listener for Bookings
        bookingsListener = database.getReference("bookings").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = mutableListOf<BookingModel>()
                for (child in snapshot.children) {
                    val booking = child.getValue(BookingModel::class.java)
                    if (booking != null) {
                        // In a real app, we'd filter by stays owned by this host
                        requests.add(booking)
                    }
                }
                _bookingRequests.value = requests.sortedByDescending { it.bookingId }
                _isLoading.value = false
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Listener for Host's Stays
        staysListener = database.getReference("stays").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val myStays = mutableListOf<StayModel>()
                for (child in snapshot.children) {
                    // Simulating host ownership check
                    val stay = child.getValue(StayModel::class.java)
                    if (stay != null) {
                        myStays.add(stay)
                    }
                }
                _listings.value = myStays
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun acceptBooking(bookingId: String) {
        database.getReference("bookings").child(bookingId).child("status").setValue("ACCEPTED")
    }

    fun rejectBooking(bookingId: String) {
        database.getReference("bookings").child(bookingId).child("status").setValue("REJECTED")
    }

    override fun onCleared() {
        super.onCleared()
        bookingsListener?.let { database.getReference("bookings").removeEventListener(it) }
        staysListener?.let { database.getReference("stays").removeEventListener(it) }
    }
}
