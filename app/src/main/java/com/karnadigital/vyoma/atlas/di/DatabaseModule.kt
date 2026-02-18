package com.karnadigital.vyoma.atlas.di

import android.content.Context
import androidx.room.Room
import com.karnadigital.vyoma.atlas.data.local.AppDatabase
import com.karnadigital.vyoma.atlas.data.local.dao.AstronomicalObjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        daoProvider: dagger.Lazy<com.karnadigital.vyoma.atlas.data.local.dao.AstronomicalObjectDao>
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "astro_db"
        )
        .addCallback(object : androidx.room.RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                com.karnadigital.vyoma.atlas.data.local.DatabaseInitializer(context) { daoProvider.get() }.initialize()
            }
        })
        .build()
    }

    @Provides
    fun provideAstronomicalObjectDao(database: AppDatabase): AstronomicalObjectDao {
        return database.astronomicalObjectDao()
    }
}
