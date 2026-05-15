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

enum class ListingStatus { NO_LISTING, CHECKLIST_INCOMPLETE, LIVE }

class HostHomeViewModel(application: Application) : AndroidViewModel(application) {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance() }

    private val _listingStatus = MutableStateFlow(ListingStatus.NO_LISTING)
    val listingStatus = _listingStatus.asStateFlow()

    private val _checklistProgress = MutableStateFlow(0f)
    val checklistProgress = _checklistProgress.asStateFlow()

    private val _pendingBookingsCount = MutableStateFlow(0)
    val pendingBookingsCount = _pendingBookingsCount.asStateFlow()

    private val _recentBookings = MutableStateFlow<List<BookingModel>>(emptyList())
    val recentBookings = _recentBookings.asStateFlow()

    private val _earningsSummary = MutableStateFlow(EarningStats(0, 0, 0, listOf(0, 0, 0, 0, 0, 0, 0)))
    val earningsSummary = _earningsSummary.asStateFlow()

    private val _activities = MutableStateFlow<List<String>>(emptyList())
    val activities = _activities.asStateFlow()

    private val _hostName = MutableStateFlow("Host")
    val hostName = _hostName.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private var bookingsListener: ValueEventListener? = null

    init {
        loadData()
    }

    private fun loadData() {
        val uid = auth.currentUser?.uid ?: run {
            _isLoading.value = false
            return
        }
        _hostName.value = auth.currentUser?.displayName ?: "Host"

        // Load checklist progress from Firebase
        database.getReference("checklist/$uid").get().addOnSuccessListener { snap ->
            val total = snap.childrenCount.toFloat().takeIf { it > 0f } ?: 17f
            val checked = snap.children.count { it.child("isChecked").getValue(Boolean::class.java) == true }.toFloat()
            val progress = checked / total
            _checklistProgress.value = progress
            _listingStatus.value = when {
                progress >= 1.0f -> ListingStatus.LIVE
                progress > 0f -> ListingStatus.CHECKLIST_INCOMPLETE
                else -> ListingStatus.NO_LISTING
            }
        }

        // Check if host has a listing
        database.getReference("stays").orderByChild("hostId").equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        if (_listingStatus.value == ListingStatus.NO_LISTING) {
                            _listingStatus.value = ListingStatus.CHECKLIST_INCOMPLETE
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // Load bookings
        bookingsListener = database.getReference("bookings")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val all = mutableListOf<BookingModel>()
                    for (child in snapshot.children) {
                        try { child.getValue(BookingModel::class.java)?.let { all.add(it) } } catch (_: Exception) {}
                    }
                    _recentBookings.value = all.take(3)
                    _pendingBookingsCount.value = all.count { it.status == "PENDING" }
                    _earningsSummary.value = EarningStats(
                        totalBookings = all.size,
                        pendingCount = all.count { it.status == "PENDING" },
                        monthlyEarning = all.filter { it.status == "ACCEPTED" }.sumOf { it.totalPrice },
                        weeklyBreakdown = listOf(2400, 3100, 1800, 4200, 2900, 3800, 2100)
                    )
                    _isLoading.value = false
                }
                override fun onCancelled(error: DatabaseError) { _isLoading.value = false }
            })

        // Load activities
        database.getReference("hosts/$uid/activities").get().addOnSuccessListener { snap ->
            val acts = mutableListOf<String>()
            snap.children.forEach { it.getValue(String::class.java)?.let { a -> acts.add(a) } }
            _activities.value = acts
        }
    }

    fun submitListing(listing: StayModel) {
        val uid = auth.currentUser?.uid ?: return
        val ref = database.getReference("stays").push()
        val withId = listing.copy(id = ref.key ?: "")
        ref.setValue(withId).addOnSuccessListener {
            _listingStatus.value = ListingStatus.CHECKLIST_INCOMPLETE
        }
    }

    fun addActivity(activity: String) {
        val current = _activities.value.toMutableList()
        if (!current.contains(activity)) {
            current.add(activity)
            _activities.value = current
            saveActivities(current)
        }
    }

    fun removeActivity(activity: String) {
        val current = _activities.value.filter { it != activity }
        _activities.value = current
        saveActivities(current)
    }

    private fun saveActivities(acts: List<String>) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("hosts/$uid/activities").setValue(acts)
    }

    override fun onCleared() {
        super.onCleared()
        bookingsListener?.let { database.getReference("bookings").removeEventListener(it) }
    }
}
