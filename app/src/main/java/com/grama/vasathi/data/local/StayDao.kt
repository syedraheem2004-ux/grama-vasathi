package com.grama.vasathi.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StayDao {
    @Query("SELECT * FROM stays")
    fun getAllStays(): Flow<List<StayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(stays: List<StayEntity>)

    @Query("DELETE FROM stays")
    fun clearAll()
}
