package com.grama.vasathi.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grama.vasathi.data.local.AppDatabase
import com.grama.vasathi.data.model.StayModel
import com.grama.vasathi.data.repository.StayRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StayViewModel(application: Application) : AndroidViewModel(application) {
    private val db by lazy { AppDatabase.getDatabase(application) }
    private val repository by lazy { StayRepository(db.stayDao()) }
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        refreshStays()
    }

    val stays: StateFlow<List<StayModel>> = combine(
        repository.stays,
        searchQuery,
        selectedFilter
    ) { stays, query, filter ->
        stays.filter { stay ->
            val matchesQuery = stay.village.contains(query, ignoreCase = true) || 
                               stay.district.contains(query, ignoreCase = true)
            val matchesFilter = if (filter == "All") true else stay.activities.contains(filter)
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshStays() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.refreshStays()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
    }
}
