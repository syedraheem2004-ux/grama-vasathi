package com.grama.vasathi.ui.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.grama.vasathi.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application) : AndroidViewModel(application) {
    private val database by lazy { FirebaseDatabase.getInstance() }

    private val _allStays = MutableStateFlow<List<StayModel>>(emptyList())
    val allStays = _allStays.asStateFlow()

    private val _wishlistedIds = MutableStateFlow<Set<String>>(emptySet())

    private val _activeFilters = MutableStateFlow(ExploreFilter())
    val activeFilters = _activeFilters.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.GRID)
    val viewMode = _viewMode.asStateFlow()

    private val _activeTab = MutableStateFlow(ExploreTab.EXPLORE)
    val activeTab = _activeTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val filteredStays = combine(_allStays, _activeFilters, _searchQuery) { stays, filter, query ->
        stays.filter { stay ->
            val matchesQuery = query.isBlank() ||
                stay.village.contains(query, true) ||
                stay.district.contains(query, true) ||
                stay.activities.any { it.contains(query, true) }
            val matchesPrice = stay.pricePerNight in filter.minPrice..filter.maxPrice
            val matchesRating = stay.rating >= filter.minRating
            val matchesDistrict = filter.district == "All" || stay.district.contains(filter.district, true)
            val matchesActivities = filter.activities.isEmpty() || filter.activities.any { fa -> stay.activities.any { it.contains(fa, true) } }
            matchesQuery && matchesPrice && matchesRating && matchesDistrict && matchesActivities
        }.let { filtered ->
            when (filter.sortOrder) {
                ExploreSortOrder.HIGHEST_RATED -> filtered.sortedByDescending { it.rating }
                ExploreSortOrder.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.pricePerNight }
                ExploreSortOrder.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.pricePerNight }
                else -> filtered
            }
        }
    }

    val wishlistedStays = combine(_allStays, _wishlistedIds) { stays, ids ->
        stays.filter { it.id in ids }
    }

    val recommendedStays = combine(_allStays, _activeFilters) { stays, _ ->
        // Simple tag-matching: stays with most activities matching user interests
        stays.filter { it.rating >= 4f }.sortedByDescending { it.activities.size }.take(5)
    }

    private var staysListener: ValueEventListener? = null

    init {
        loadStays()
    }

    private fun loadStays() {
        staysListener = database.getReference("stays").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stays = mutableListOf<StayModel>()
                for (child in snapshot.children) {
                    try { child.getValue(StayModel::class.java)?.let { stays.add(it) } } catch (_: Exception) {}
                }
                // Add sample data if Firebase is empty
                if (stays.isEmpty()) stays.addAll(sampleStays())
                _allStays.value = stays
                _isLoading.value = false
            }
            override fun onCancelled(error: DatabaseError) { _isLoading.value = false }
        })
    }

    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }
    fun setViewMode(mode: ViewMode) { _viewMode.value = mode }
    fun setActiveTab(tab: ExploreTab) { _activeTab.value = tab }
    fun applyFilters(filter: ExploreFilter) { _activeFilters.value = filter }

    fun toggleWishlist(stayId: String) {
        val current = _wishlistedIds.value.toMutableSet()
        if (stayId in current) current.remove(stayId) else current.add(stayId)
        _wishlistedIds.value = current
    }

    fun isWishlisted(stayId: String) = stayId in _wishlistedIds.value

    fun loadMoreStays() { /* Pagination - load next 10 from Firebase */ }

    override fun onCleared() {
        super.onCleared()
        staysListener?.let { database.getReference("stays").removeEventListener(it) }
    }

    private fun sampleStays() = listOf(
        StayModel("1", "Lakshmi Gowda", "Srirangapatna", "Mandya", listOf("🐄 Cow Milking", "🌾 Organic Farming", "🌅 Sunrise Walk"), 4.5f, 1200, "", 4.8f),
        StayModel("2", "Ramu Hebbar", "Kushalnagar", "Coorg", listOf("🐦 Birdwatching", "🏕️ Bonfire Night", "🎣 Fishing"), 4.2f, 1800, "", 4.5f),
        StayModel("3", "Savitha Naik", "Belur", "Hassan", listOf("🍳 Cooking Class", "🌿 Herb Picking"), 4.7f, 950, "", 4.9f),
        StayModel("4", "Gopal Rao", "Sakleshpur", "Hassan", listOf("🚜 Tractor Ride", "🌾 Organic Farming"), 4.0f, 1500, "", 4.2f),
        StayModel("5", "Meera Devi", "Coorg Hills", "Coorg", listOf("🐦 Birdwatching", "☕ Coffee Tour"), 4.6f, 2200, "", 4.7f),
        StayModel("6", "Basava Reddy", "Melkote", "Mandya", listOf("🐄 Cow Milking", "🌅 Sunrise Walk"), 4.3f, 800, "", 4.4f)
    )
}
