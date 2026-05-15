package com.grama.vasathi.data.model

data class ChecklistSection(
    val sectionId: String,
    val title: String,
    val icon: String,
    val items: List<ChecklistItem>
)

data class ChecklistItem(
    val itemId: String,
    val label: String,
    val isChecked: Boolean,
    val proofImageUrl: String? = null
)

enum class ReadinessStatus {
    NOT_READY, ALMOST_READY, GUEST_READY
}
