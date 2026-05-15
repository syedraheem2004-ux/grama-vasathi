package com.grama.vasathi.ui.booking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.grama.vasathi.data.local.AppDatabase
import com.grama.vasathi.data.model.BookingModel
import com.grama.vasathi.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class BookingUiState {
    object Idle : BookingUiState()
    object Loading : BookingUiState()
    data class Success(val bookingId: String) : BookingUiState()
    data class Error(val message: String) : BookingUiState()
}

class BookingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BookingRepository
    
    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _checkInDate = MutableStateFlow<Long?>(null)
    val checkInDate = _checkInDate.asStateFlow()

    private val _checkOutDate = MutableStateFlow<Long?>(null)
    val checkOutDate = _checkOutDate.asStateFlow()

    private val _guestCount = MutableStateFlow(1)
    val guestCount = _guestCount.asStateFlow()

    init {
        val bookingDao = AppDatabase.getDatabase(application).bookingDao()
        repository = BookingRepository(bookingDao)
    }

    fun onCheckInDateSelected(date: Long?) {
        _checkInDate.value = date
    }

    fun onCheckOutDateSelected(date: Long?) {
        _checkOutDate.value = date
    }

    fun incrementGuests() {
        if (_guestCount.value < 6) _guestCount.value++
    }

    fun decrementGuests() {
        if (_guestCount.value > 1) _guestCount.value--
    }

    fun calculateTotal(pricePerNight: Int): Int {
        val inDate = _checkInDate.value ?: return 0
        val outDate = _checkOutDate.value ?: return 0
        val nights = ((outDate - inDate) / (1000 * 60 * 60 * 24)).toInt()
        return if (nights > 0) pricePerNight * nights * _guestCount.value else 0
    }

    fun submitBooking(stayId: String, pricePerNight: Int) {
        val guestId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val checkIn = _checkInDate.value ?: return
        val checkOut = _checkOutDate.value ?: return
        
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            
            val bookingId = generateBookingId()
            val totalPrice = calculateTotal(pricePerNight)
            
            val booking = BookingModel(
                bookingId = bookingId,
                stayId = stayId,
                guestId = guestId,
                checkIn = formatDate(checkIn),
                checkOut = formatDate(checkOut),
                guestCount = _guestCount.value,
                totalPrice = totalPrice,
                status = "PENDING"
            )
            
            val result = repository.submitBooking(booking)
            if (result) {
                _uiState.value = BookingUiState.Success(bookingId)
            } else {
                _uiState.value = BookingUiState.Error("Failed to submit booking. Please try again.")
            }
        }
    }

    private fun generateBookingId(): String {
        val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val random = (1..4).map { (('A'..'Z') + ('0'..'9')).random() }.joinToString("")
        return "GV-$date-$random"
    }

    private fun formatDate(timeInMillis: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timeInMillis))
    }
    
    fun resetState() {
        _uiState.value = BookingUiState.Idle
    }
}
