package com.grama.vasathi.ui.roleselection

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class UserRole {
    HOST, GUEST
}

class RoleSelectionViewModel : ViewModel() {
    private val _selectedRole = MutableStateFlow<UserRole?>(null)
    val selectedRole: StateFlow<UserRole?> = _selectedRole.asStateFlow()

    fun selectRole(role: UserRole) {
        _selectedRole.value = role
    }
}
