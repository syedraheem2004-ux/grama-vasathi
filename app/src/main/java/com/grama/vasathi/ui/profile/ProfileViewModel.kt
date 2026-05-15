package com.grama.vasathi.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.grama.vasathi.data.model.AppSettings
import com.grama.vasathi.data.model.UserProfile
import com.grama.vasathi.ui.roleselection.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance() }
    
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile = _userProfile.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings = _appSettings.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val user = auth.currentUser ?: return
        
        database.getReference("users").child(user.uid).get().addOnSuccessListener { snapshot ->
            try {
                val profile = snapshot.getValue(UserProfile::class.java) ?: UserProfile(
                    userId = user.uid,
                    fullName = user.displayName ?: "Guest User",
                    email = user.email ?: "",
                    role = "GUEST",
                    memberSince = "April 2024",
                    bio = "Adventure seeker and nature lover from Bengaluru."
                )
                _userProfile.value = profile
            } catch (e: Exception) {
                // Fallback on parse error
                _userProfile.value = UserProfile(userId = user.uid, email = user.email ?: "")
            }
            _isLoading.value = false
        }.addOnFailureListener {
            _isLoading.value = false
        }
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    fun updateProfile(updatedProfile: UserProfile) {
        viewModelScope.launch {
            database.getReference("users").child(updatedProfile.userId).setValue(updatedProfile)
                .addOnSuccessListener {
                    _userProfile.value = updatedProfile
                    _isEditMode.value = false
                }
        }
    }

    fun updateSetting(isDarkMode: Boolean? = null, notifications: Boolean? = null) {
        _appSettings.value = _appSettings.value.copy(
            isDarkMode = isDarkMode ?: _appSettings.value.isDarkMode,
            notificationsEnabled = notifications ?: _appSettings.value.notificationsEnabled
        )
    }

    fun logOut() {
        auth.signOut()
    }

    fun deleteAccount(confirmText: String, onSuccess: () -> Unit) {
        if (confirmText == "DELETE") {
            val user = auth.currentUser ?: return
            database.getReference("users").child(user.uid).removeValue()
            user.delete().addOnSuccessListener {
                onSuccess()
            }
        }
    }
}
