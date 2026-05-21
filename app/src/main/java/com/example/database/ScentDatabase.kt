package com.example.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        BrandEntity::class,
        PerfumeProduct::class,
        ProductVariantEntity::class,
        StoreOfferEntity::class,
        PriceSnapshotEntity::class,
        UserPreferencesEntity::class,
        NotificationLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ScentDatabase : RoomDatabase() {
    abstract fun scentDao(): ScentDao
}
