package com.karnadigital.vyoma.atlas.data.repository

import com.karnadigital.vyoma.atlas.data.local.dao.AstronomicalObjectDao
import com.karnadigital.vyoma.atlas.data.local.entity.AstronomicalObject
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AstronomyRepository @Inject constructor(
    private val astronomicalObjectDao: AstronomicalObjectDao
) {
    fun getAllObjects(): Flow<List<AstronomicalObject>> {
        return astronomicalObjectDao.getAllObjects()
    }

    suspend fun getObjectById(id: String): AstronomicalObject? {
        return astronomicalObjectDao.getObjectById(id)
    }

    fun getObjectsByType(type: String): Flow<List<AstronomicalObject>> {
        return astronomicalObjectDao.getObjectsByType(type)
    }

    fun getObjectsByParentId(parentId: String): Flow<List<AstronomicalObject>> {
        return astronomicalObjectDao.getObjectsByParentId(parentId)
    }
}
