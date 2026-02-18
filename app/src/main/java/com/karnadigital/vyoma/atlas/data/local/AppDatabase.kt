package com.karnadigital.vyoma.atlas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.karnadigital.vyoma.atlas.data.local.dao.AstronomicalObjectDao
import com.karnadigital.vyoma.atlas.data.local.entity.AstronomicalObject

@Database(entities = [AstronomicalObject::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun astronomicalObjectDao(): AstronomicalObjectDao
}
