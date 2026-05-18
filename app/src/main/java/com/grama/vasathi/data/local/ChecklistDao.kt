package com.grama.vasathi.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_items")
    fun getAllItems(): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ChecklistItemEntity>): List<Long>

    @Query("UPDATE checklist_items SET isChecked = :isChecked WHERE itemId = :itemId")
    suspend fun updateItemStatus(itemId: String, isChecked: Boolean): Int
}
