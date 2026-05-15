package com.grama.vasathi.data.model

data class EarningStats(
    val totalBookings: Int = 0,
    val pendingCount: Int = 0,
    val monthlyEarning: Int = 0,
    val weeklyBreakdown: List<Int> = listOf(0, 0, 0, 0) // 4 values for each week
)
