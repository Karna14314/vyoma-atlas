package com.karnadigital.vyoma.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.karnadigital.vyoma.atlas.data.local.entity.AstronomicalObject
import kotlinx.coroutines.flow.Flow

@Dao
interface AstronomicalObjectDao {
    @Query("SELECT * FROM astronomical_objects")
    fun getAllObjects(): Flow<List<AstronomicalObject>>

    @Query("SELECT * FROM astronomical_objects WHERE type = :type")
    fun getObjectsByType(type: String): Flow<List<AstronomicalObject>>

    @Query("SELECT * FROM astronomical_objects WHERE id = :id")
    suspend fun getObjectById(id: String): AstronomicalObject?

    @Query("SELECT * FROM astronomical_objects WHERE parentId = :parentId")
    fun getObjectsByParentId(parentId: String): Flow<List<AstronomicalObject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(objects: List<AstronomicalObject>)
}
