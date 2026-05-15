package com.grama.vasathi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grama.vasathi.data.model.StayModel

@Entity(tableName = "stays")
data class StayEntity(
    @PrimaryKey val id: String,
    val hostName: String,
    val village: String,
    val district: String,
    val activities: List<String>,
    val hygieneScore: Float,
    val pricePerNight: Int,
    val imageUrl: String,
    val rating: Float
)

fun StayEntity.toModel() = StayModel(
    id = id,
    hostName = hostName,
    village = village,
    district = district,
    activities = activities,
    hygieneScore = hygieneScore,
    pricePerNight = pricePerNight,
    imageUrl = imageUrl,
    rating = rating
)

fun StayModel.toEntity() = StayEntity(
    id = id,
    hostName = hostName,
    village = village,
    district = district,
    activities = activities,
    hygieneScore = hygieneScore,
    pricePerNight = pricePerNight,
    imageUrl = imageUrl,
    rating = rating
)
