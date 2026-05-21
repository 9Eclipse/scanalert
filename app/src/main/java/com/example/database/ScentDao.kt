package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScentDao {
    @Query("SELECT * FROM brands ORDER BY name ASC")
    fun getBrands(): Flow<List<BrandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrand(brand: BrandEntity)

    @Query("UPDATE brands SET isFavorite = :isFavorite WHERE name = :name")
    suspend fun updateFavoriteBrand(name: String, isFavorite: Boolean)

    @Query("SELECT * FROM perfume_products")
    fun getPerfumeProducts(): Flow<List<PerfumeProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: PerfumeProduct)

    @Query("SELECT * FROM product_variants")
    fun getProductVariants(): Flow<List<ProductVariantEntity>>

    @Query("SELECT * FROM product_variants WHERE isWatched = 1")
    fun getWatchedVariants(): Flow<List<ProductVariantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariant(variant: ProductVariantEntity)

    @Query("UPDATE product_variants SET isWatched = :isWatched WHERE id = :id")
    suspend fun updateWatchVariant(id: Int, isWatched: Boolean)

    @Query("SELECT * FROM store_offers WHERE variantId = :variantId")
    fun getStoreOffersForVariant(variantId: Int): Flow<List<StoreOfferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoreOffer(offer: StoreOfferEntity)

    @Query("SELECT * FROM price_snapshots WHERE variantId = :variantId ORDER BY timestamp ASC")
    fun getPriceSnapshotsForVariant(variantId: Int): Flow<List<PriceSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceSnapshot(snapshot: PriceSnapshotEntity)

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getUserPreferencesFlow(): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getUserPreferences(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferencesEntity)

    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getNotificationLogs(): Flow<List<NotificationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationLog(log: NotificationLog)

    @Query("UPDATE notification_logs SET read = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    @Query("DELETE FROM notification_logs WHERE id = :id")
    suspend fun deleteNotification(id: Int)

    @Query("DELETE FROM notification_logs")
    suspend fun clearAllNotifications()
}
