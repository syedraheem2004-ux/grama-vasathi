package com.grama.vasathi.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.grama.vasathi.ui.roleselection.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Login failed")
            }
        }
    }

    fun register(name: String, email: String, phone: String, location: String, password: String) {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || location.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // Register user
                auth.createUserWithEmailAndPassword(email, password).await()
                
                // Here we would typically save additional profile info (name, phone, location, role)
                // to Firebase Realtime Database or Firestore. For now, we just succeed.
                
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Registration failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
