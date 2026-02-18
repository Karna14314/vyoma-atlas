package com.karnadigital.vyoma.atlas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "astronomical_objects")
data class AstronomicalObject(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String, // STAR, PLANET, MOON, GALAXY, NEBULA, CONSTELLATION
    val description: String?,
    val distanceAu: Double?,
    val distanceLy: Double?,
    val radiusKm: Double?,
    val magnitude: Double?,
    val constellation: String?,
    val imageUrl: String?, // Can be http URL or local asset path
    val rightAscension: Double?, // In degrees (0-360)
    val declination: Double?,     // In degrees (-90 to +90)
    val parentId: String? = null,
    val category: String? = null // "Solar System", "Stars", "Constellations", "Deep Sky"
)
