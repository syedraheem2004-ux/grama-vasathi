package com.grama.vasathi.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.grama.vasathi.data.local.StayDao
import com.grama.vasathi.data.local.StayEntity
import com.grama.vasathi.data.local.toEntity
import com.grama.vasathi.data.local.toModel
import com.grama.vasathi.data.model.StayModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class StayRepository(private val stayDao: StayDao) {
    private val database by lazy { FirebaseDatabase.getInstance().getReference("stays") }

    val stays: Flow<List<StayModel>> = stayDao.getAllStays().map { entities ->
        entities.map { it.toModel() }
    }

    suspend fun refreshStays() = withContext(Dispatchers.IO) {
        try {
            val snapshot = database.get().await()
            val remoteStays = mutableListOf<StayModel>()
            
            for (child in snapshot.children) {
                try {
                    val id = child.key ?: ""
                    val hostName = child.child("hostName").value?.toString() ?: ""
                    val village = child.child("village").value?.toString() ?: ""
                    val district = child.child("district").value?.toString() ?: ""
                    val activities = mutableListOf<String>()
                    child.child("activities").children.forEach { activity ->
                        activity.value?.toString()?.let { activities.add(it) }
                    }
                    val hygieneScore = child.child("hygieneScore").value?.toString()?.toFloatOrNull() ?: 0f
                    val pricePerNight = child.child("pricePerNight").value?.toString()?.toIntOrNull() ?: 0
                    val imageUrl = child.child("imageUrl").value?.toString() ?: ""
                    val rating = child.child("rating").value?.toString()?.toFloatOrNull() ?: 0f
                    
                    remoteStays.add(StayModel(id, hostName, village, district, activities, hygieneScore, pricePerNight, imageUrl, rating))
                } catch (e: Exception) {
                    // Skip malformed stay
                }
            }
            
            if (remoteStays.isNotEmpty()) {
                stayDao.clearAll()
                stayDao.insertAll(remoteStays.map { it.toEntity() })
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}
