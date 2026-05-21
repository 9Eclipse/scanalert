package com.example.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var database: ScentDatabase? = null
    private var repository: ScentRepository? = null

    fun getDatabase(context: Context): ScentDatabase {
        return database ?: synchronized(this) {
            val db = Room.databaseBuilder(
                context.applicationContext,
                ScentDatabase::class.java,
                "scentalert_db"
            ).fallbackToDestructiveMigration()
             .build()
            database = db
            db
        }
    }

    fun getRepository(context: Context): ScentRepository {
        return repository ?: synchronized(this) {
            val repo = ScentRepository(getDatabase(context).scentDao())
            repository = repo
            repo
        }
    }
}
