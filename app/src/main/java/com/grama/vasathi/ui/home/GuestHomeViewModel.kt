package com.grama.vasathi.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.grama.vasathi.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GuestHomeViewModel(application: Application) : AndroidViewModel(application) {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance() }

    private val _guestProfile = MutableStateFlow(UserProfile())
    val guestProfile = _guestProfile.asStateFlow()

    private val _allStays = MutableStateFlow<List<StayModel>>(emptyList())
    private val _wishlistedIds = MutableStateFlow<Set<String>>(emptySet())

    private val _recommendedStays = MutableStateFlow<List<StayModel>>(emptyList())
    val recommendedStays = _recommendedStays.asStateFlow()

    private val _nearbyStays = MutableStateFlow<List<StayModel>>(emptyList())
    val nearbyStays = _nearbyStays.asStateFlow()

    private val _topRatedStays = MutableStateFlow<List<StayModel>>(emptyList())
    val topRatedStays = _topRatedStays.asStateFlow()

    private val _recentlyViewed = MutableStateFlow<List<RecentlyViewedModel>>(emptyList())
    val recentlyViewed = _recentlyViewed.asStateFlow()

    private val _upcomingBooking = MutableStateFlow<BookingModel?>(null)
    val upcomingBooking = _upcomingBooking.asStateFlow()

    private val _guestStats = MutableStateFlow(GuestStats())
    val guestStats = _guestStats.asStateFlow()

    private val _seasonalOffers = MutableStateFlow<List<OfferModel>>(emptyList())
    val seasonalOffers = _seasonalOffers.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _isLoading.value = true
        // Load Profile
        auth.currentUser?.uid?.let { uid ->
            database.getReference("users/$uid").get().addOnSuccessListener { snap ->
                snap.getValue(UserProfile::class.java)?.let { _guestProfile.value = it }
            }
        }

        // Load Stays
        database.getReference("stays").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stays = mutableListOf<StayModel>()
                for (child in snapshot.children) {
                    try { child.getValue(StayModel::class.java)?.let { stays.add(it) } } catch (_: Exception) {}
                }
                if (stays.isEmpty()) stays.addAll(sampleStays())
                _allStays.value = stays

                _nearbyStays.value = stays.filter { it.district == "Mandya" || it.district == "Mysuru" }.take(5)
                _topRatedStays.value = stays.filter { it.rating >= 4.5f }.sortedByDescending { it.rating }.take(3)
                getRecommendations()
                _isLoading.value = false
            }
            override fun onCancelled(error: DatabaseError) { _isLoading.value = false }
        })

        // Sample Offers
        _seasonalOffers.value = listOf(
            OfferModel("1", "Summer Special", "Up to 20% off farm stays in Coorg!", 20, "Coorg", "2026-06-30", "")
        )

        // Sample Stats
        _guestStats.value = GuestStats(staysCompleted = 5, savedStays = 8, reviewsGiven = 4)

        // Sample Recently Viewed
        _recentlyViewed.value = listOf(
            RecentlyViewedModel("1", "Lakshmi Farm Stay", "Srirangapatna", 1200, "", "10 mins ago"),
            RecentlyViewedModel("3", "Savitha's Farm", "Belur", 950, "", "1 hr ago")
        )

        // Sample Upcoming Booking - Fixed parameter mapping
        _upcomingBooking.value = BookingModel(
            bookingId = "b1",
            stayId = "1",
            guestId = "g1",
            checkIn = "2026-06-15",
            checkOut = "2026-06-18",
            guestCount = 2,
            totalPrice = 3600,
            status = "CONFIRMED"
        )
    }

    fun getRecommendations(guestId: String = "") {
        val stays = _allStays.value
        _recommendedStays.value = stays.filter { it.rating >= 4f }.shuffled().take(5)
    }

    fun toggleWishlist(stayId: String) {
        val current = _wishlistedIds.value.toMutableSet()
        if (stayId in current) current.remove(stayId) else current.add(stayId)
        _wishlistedIds.value = current
    }

    fun isWishlisted(stayId: String) = stayId in _wishlistedIds.value

    fun clearRecentlyViewed() {
        _recentlyViewed.value = emptyList()
    }

    private fun sampleStays() = listOf(
        StayModel("1", "Lakshmi Gowda", "Srirangapatna", "Mandya", listOf("🐄 Cow Milking", "🌾 Organic Farming"), 4.5f, 1200, "", 4.8f),
        StayModel("2", "Ramu Hebbar", "Kushalnagar", "Coorg", listOf("🐦 Birdwatching", "🏕️ Bonfire Night"), 4.2f, 1800, "", 4.5f),
        StayModel("3", "Savitha Naik", "Belur", "Hassan", listOf("🍳 Cooking Class", "🌿 Herb Picking"), 4.7f, 950, "", 4.9f)
    )
}
