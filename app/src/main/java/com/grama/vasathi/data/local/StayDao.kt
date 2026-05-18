package com.grama.vasathi.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StayDao {
    @Query("SELECT * FROM stays")
    fun getAllStays(): Flow<List<StayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stays: List<StayEntity>): List<Long>

    @Query("DELETE FROM stays")
    suspend fun clearAll(): Int
}
