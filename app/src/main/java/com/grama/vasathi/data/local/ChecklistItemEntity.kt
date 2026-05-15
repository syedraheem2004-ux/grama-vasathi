package com.grama.vasathi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey val itemId: String,
    val sectionId: String,
    val label: String,
    val isChecked: Boolean,
    val proofImageUrl: String? = null
)
