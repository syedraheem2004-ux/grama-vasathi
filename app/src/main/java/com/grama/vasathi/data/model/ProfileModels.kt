package com.grama.vasathi.data.model

import com.grama.vasathi.ui.roleselection.UserRole

data class UserProfile(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,
    val role: String = "GUEST",
    val memberSince: String = "",
    val isVerified: Boolean = false
)

data class AppSettings(
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val language: String = "English",
    val locationEnabled: Boolean = true
)
