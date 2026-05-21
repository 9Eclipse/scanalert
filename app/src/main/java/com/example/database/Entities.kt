package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "brands")
data class BrandEntity(
    @PrimaryKey val name: String,
    val searchAlias: String = "",
    val isFavorite: Boolean = false,
    val imageUrl: String = ""
) : Serializable

@Entity(tableName = "perfume_products")
data class PerfumeProduct(
    @PrimaryKey val id: Int,
    val name: String,
    val brand: String,
    val description: String = "",
    val imageUrl: String = ""
) : Serializable

@Entity(tableName = "product_variants")
data class ProductVariantEntity(
    @PrimaryKey val id: Int,
    val productId: Int,
    val concentration: String, // EDP, EDT, Parfum, Extrait
    val capacityMl: Int,
    val isTester: Boolean = false,
    val isGiftSet: Boolean = false,
    val currentPrice: Float,
    val originalPrice: Float,
    val dealScore: Int, // 0 - 100
    val directLink: String = "",
    val isWatched: Boolean = false
) : Serializable {
    val discountPercent: Int
        get() = if (originalPrice > 0) {
            (((originalPrice - currentPrice) / originalPrice) * 100).toInt()
        } else 0

    val pricePerMl: Float
        get() = if (capacityMl > 0) currentPrice / capacityMl else 0f
}

@Entity(tableName = "store_offers")
data class StoreOfferEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val variantId: Int,
    val storeName: String, // Notino, Sephora, Douglas, Flaconi, Hebe
    val price: Float,
    val directLink: String,
    val isAvailable: Boolean = true
) : Serializable

@Entity(tableName = "price_snapshots")
data class PriceSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val variantId: Int,
    val timestamp: Long,
    val price: Float
) : Serializable

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val minDiscountPercent: Int = 20,
    val excludeTesters: Boolean = true,
    val excludeGiftSets: Boolean = false,
    val enableQuietHours: Boolean = false,
    val quietHoursFrom: String = "22:00",
    val quietHoursTo: String = "07:00",
    val maxPrice: Int = 800,
    val preferredCapacities: String = "50,100" // comma separated List
) : Serializable

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val variantId: Int,
    val read: Boolean = false
) : Serializable
