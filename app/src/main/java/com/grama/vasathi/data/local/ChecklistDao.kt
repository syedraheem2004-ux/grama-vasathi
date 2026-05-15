package com.grama.vasathi.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_items")
    fun getAllItems(): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(items: List<ChecklistItemEntity>)

    @Query("UPDATE checklist_items SET isChecked = :isChecked WHERE itemId = :itemId")
    fun updateItemStatus(itemId: String, isChecked: Boolean)
}
