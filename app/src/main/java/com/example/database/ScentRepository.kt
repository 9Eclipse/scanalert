package com.example.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class ScentRepository(private val dao: ScentDao) {

    val brands: Flow<List<BrandEntity>> = dao.getBrands()
    val perfumeProducts: Flow<List<PerfumeProduct>> = dao.getPerfumeProducts()
    val productVariants: Flow<List<ProductVariantEntity>> = dao.getProductVariants()
    val watchedVariants: Flow<List<ProductVariantEntity>> = dao.getWatchedVariants()
    val userPreferences: Flow<UserPreferencesEntity?> = dao.getUserPreferencesFlow()
    val notifications: Flow<List<NotificationLog>> = dao.getNotificationLogs()

    suspend fun updateFavoriteBrand(name: String, isFavorite: Boolean) {
        dao.updateFavoriteBrand(name, isFavorite)
    }

    suspend fun updateWatchVariant(id: Int, isWatched: Boolean) {
        dao.updateWatchVariant(id, isWatched)
    }

    fun getStoreOffersForVariant(variantId: Int): Flow<List<StoreOfferEntity>> {
        return dao.getStoreOffersForVariant(variantId)
    }

    fun getPriceSnapshotsForVariant(variantId: Int): Flow<List<PriceSnapshotEntity>> {
        return dao.getPriceSnapshotsForVariant(variantId)
    }

    suspend fun savePreferences(prefs: UserPreferencesEntity) {
        dao.insertUserPreferences(prefs)
    }

    suspend fun markNotificationAsRead(id: Int) {
        dao.markNotificationAsRead(id)
    }

    suspend fun deleteNotification(id: Int) {
        dao.deleteNotification(id)
    }

    suspend fun clearAllNotifications() {
        dao.clearAllNotifications()
    }

    // Pre-seeds a highly realistic, premium perfume deal dataset on first launch
    suspend fun preseedDatabaseIfEmpty() {
        val existingPrefs = dao.getUserPreferences()
        if (existingPrefs != null) return // Already seeded!

        // 1. User Prefs
        dao.insertUserPreferences(UserPreferencesEntity())

        // 2. Premium Brands
        val defaultBrands = listOf(
            BrandEntity("Versace", "versace", true, "https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=300"),
            BrandEntity("Creed", "creed", true, "https://images.unsplash.com/photo-1594035910387-fea47794261f?w=300"),
            BrandEntity("Dior", "dior dgr", true, "https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?w=300"),
            BrandEntity("Tom Ford", "tom ford tf", true, "https://images.unsplash.com/photo-1523293182086-7651a899d37f?w=300"),
            BrandEntity("Chanel", "chanel ch", false, "https://images.unsplash.com/photo-1541643600914-78b084683601?w=300"),
            BrandEntity("Armani", "giorgio armani ga", false, "https://images.unsplash.com/photo-1615655096345-61a54750068d?w=300")
        )
        defaultBrands.forEach { dao.insertBrand(it) }

        // 3. Products
        val products = listOf(
            PerfumeProduct(1, "Aventus", "Creed", "Kultowy zapach elegancji, charakteryzujący się nutami ananasa, brzozy i mchu dębowego. Prawdziwa korona męskiej perfumerii.", "https://images.unsplash.com/photo-1594035910387-fea47794261f?w=500"),
            PerfumeProduct(2, "Sauvage", "Dior", "Szlachetny, surowy i wyjątkowo świeży zapach z wyczuwalną kalabryjską bergamotką i drzewnym akordem ambroksanu.", "https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?w=500"),
            PerfumeProduct(3, "Lost Cherry", "Tom Ford", "Pełna kontrastów, słodko-cierpka podróż do serca dojrzałej, likierowej wiśni zmieszanej z gorzkim migdałem i turecką różą.", "https://images.unsplash.com/photo-1523293182086-7651a899d37f?w=500"),
            PerfumeProduct(4, "Bleu de Chanel", "Chanel", "Aromatyczno-drzewny zapach o intensywnej zmysłowości, łączący świeżość cytrusów z magnetycznym ciepłem cedru i sandałowca.", "https://images.unsplash.com/photo-1541643600914-78b084683601?w=500"),
            PerfumeProduct(5, "Eros", "Versace", "Miłość, pasja, piękno i pożądanie. Świeża, orientalna aura mięty, zielonego jabłka, włoskiego cytryny i wenezuelskiego bobu tonka.", "https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=500"),
            PerfumeProduct(6, "Acqua Di Gio", "Armani", "Czysta harmonia morskiego wiatru, dojrzałych cytrusów oraz ciepłego piasku, uosabiająca klasyczną, rześką świeżość.", "https://images.unsplash.com/photo-1615655096345-61a54750068d?w=500")
        )
        products.forEach { dao.insertProduct(it) }

        // 4. Product Variants (original vs promo, Deal scores, volumes)
        val variants = listOf(
            ProductVariantEntity(101, 1, "Eau de Parfum", 100, false, false, 849f, 1250f, 88, "https://www.notino.pl", false),
            ProductVariantEntity(102, 2, "Eau de Toilette", 100, false, false, 339f, 520f, 84, "https://www.notino.pl", true),
            ProductVariantEntity(103, 3, "Eau de Parfum", 50, false, false, 949f, 1420f, 78, "https://www.notino.pl", false),
            ProductVariantEntity(104, 4, "Eau de Parfum", 100, false, false, 449f, 650f, 75, "https://www.notino.pl", false),
            ProductVariantEntity(105, 5, "Eau de Toilette", 100, false, false, 199f, 390f, 96, "https://www.notino.pl", true), // Outstanding deal!
            ProductVariantEntity(106, 6, "Eau de Toilette", 200, false, false, 420f, 580f, 58, "https://www.notino.pl", false)
        )
        variants.forEach { dao.insertVariant(it) }

        // 5. Store Competitor pricing comparison list
        val storeOffers = listOf(
            // Creed Aventus Offers
            StoreOfferEntity(0, 101, "Notino", 849f, "https://www.notino.pl"),
            StoreOfferEntity(0, 101, "Douglas", 1120f, "https://www.douglas.pl"),
            StoreOfferEntity(0, 101, "Sephora", 1250f, "https://www.sephora.pl"),
            StoreOfferEntity(0, 101, "Flaconi", 980f, "https://www.flaconi.pl"),

            // Dior Sauvage Offers
            StoreOfferEntity(0, 102, "Notino", 339f, "https://www.notino.pl"),
            StoreOfferEntity(0, 102, "Douglas", 489f, "https://www.douglas.pl"),
            StoreOfferEntity(0, 102, "Sephora", 519f, "https://www.sephora.pl"),
            StoreOfferEntity(0, 102, "Hebe", 399f, "https://www.hebe.pl"),

            // Tom Ford Lost Cherry Offers
            StoreOfferEntity(0, 103, "Notino", 949f, "https://www.notino.pl"),
            StoreOfferEntity(0, 103, "Douglas", 1350f, "https://www.douglas.pl"),
            StoreOfferEntity(0, 103, "Sephora", 1420f, "https://www.sephora.pl"),

            // Chanel Bleu Offers
            StoreOfferEntity(0, 104, "Notino", 449f, "https://www.notino.pl"),
            StoreOfferEntity(0, 104, "Douglas", 620f, "https://www.douglas.pl"),
            StoreOfferEntity(0, 104, "Sephora", 650f, "https://www.sephora.pl"),

            // Versace Eros (Incredible sale at Flaconi/Notino!)
            StoreOfferEntity(0, 105, "Notino", 199f, "https://www.notino.pl"),
            StoreOfferEntity(0, 105, "Flaconi", 209f, "https://www.flaconi.pl"),
            StoreOfferEntity(0, 105, "Douglas", 369f, "https://www.douglas.pl"),
            StoreOfferEntity(0, 105, "Hebe", 289f, "https://www.hebe.pl"),

            // Acqua Di Gio
            StoreOfferEntity(0, 106, "Notino", 420f, "https://www.notino.pl"),
            StoreOfferEntity(0, 106, "Super-Pharm", 459f, "https://www.superpharm.pl"),
            StoreOfferEntity(0, 106, "Douglas", 580f, "https://www.douglas.pl")
        )
        storeOffers.forEach { dao.insertStoreOffer(it) }

        // 6. Seeding Price histories for Graph drawing matching actual price curves
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        
        // Seed 5 snapshots for each
        val histories = listOf(
            // Creed Aventus - stable, then dropped
            PriceSnapshotEntity(0, 101, now - 30 * oneDay, 1200f),
            PriceSnapshotEntity(0, 101, now - 20 * oneDay, 1150f),
            PriceSnapshotEntity(0, 101, now - 15 * oneDay, 1150f),
            PriceSnapshotEntity(0, 101, now - 7 * oneDay, 999f),
            PriceSnapshotEntity(0, 101, now, 849f),

            // Dior Sauvage - fluctuating, dropped
            PriceSnapshotEntity(0, 102, now - 30 * oneDay, 499f),
            PriceSnapshotEntity(0, 102, now - 20 * oneDay, 450f),
            PriceSnapshotEntity(0, 102, now - 15 * oneDay, 480f),
            PriceSnapshotEntity(0, 102, now - 5 * oneDay, 399f),
            PriceSnapshotEntity(0, 102, now, 339f),

            // Tom Ford Lost Cherry - slow decline
            PriceSnapshotEntity(0, 103, now - 30 * oneDay, 1380f),
            PriceSnapshotEntity(0, 103, now - 20 * oneDay, 1290f),
            PriceSnapshotEntity(0, 103, now - 12 * oneDay, 1190f),
            PriceSnapshotEntity(0, 103, now - 4 * oneDay, 1100f),
            PriceSnapshotEntity(0, 103, now, 949f),

            // Chanel Bleu
            PriceSnapshotEntity(0, 104, now - 30 * oneDay, 620f),
            PriceSnapshotEntity(0, 104, now - 18 * oneDay, 599f),
            PriceSnapshotEntity(0, 104, now - 10 * oneDay, 550f),
            PriceSnapshotEntity(0, 104, now - 3 * oneDay, 480f),
            PriceSnapshotEntity(0, 104, now, 449f),

            // Versace Eros - massive price drop!
            PriceSnapshotEntity(0, 105, now - 30 * oneDay, 370f),
            PriceSnapshotEntity(0, 105, now - 20 * oneDay, 350f),
            PriceSnapshotEntity(0, 105, now - 14 * oneDay, 350f),
            PriceSnapshotEntity(0, 105, now - 6 * oneDay, 249f),
            PriceSnapshotEntity(0, 105, now, 199f),

            // Acqua Di Gio - regular promo fluctuation
            PriceSnapshotEntity(0, 106, now - 30 * oneDay, 550f),
            PriceSnapshotEntity(0, 106, now - 20 * oneDay, 520f),
            PriceSnapshotEntity(0, 106, now - 10 * oneDay, 520f),
            PriceSnapshotEntity(0, 106, now - 4 * oneDay, 435f),
            PriceSnapshotEntity(0, 106, now, 420f)
        )
        histories.forEach { dao.insertPriceSnapshot(it) }

        // Initial Notification list to explain the push engine system immediately
        dao.insertNotificationLog(
            NotificationLog(
                title = "Wykryto rekordowe przeceny!",
                body = "Cena Versace Eros spadła o 49%! Aktualnie najtańsza oferta na rynku to 199 PLN w Notino. Deal Score: 96/100.",
                variantId = 105
            )
        )
    }

    // Dynamic simulateScraping: picks a random product, simulates a 20% to 50% price drop at Notino or Sephora,
    // stores snapshot, updates currentPrice and triggers a real NotificationLog in the local DB so state flow refreshes instantly!
    suspend fun simulateScraping(): NotificationLog {
        // Find existing variants
        val pList = dao.getPerfumeProducts().firstOrNull() ?: emptyList()
        val vList = dao.getProductVariants().firstOrNull() ?: emptyList()
        if (pList.isEmpty() || vList.isEmpty()) {
            throw IllegalStateException("Database is empty, cannot simulate scraping yet")
        }

        // Select a random product variant index
        val targetIndex = Random.nextInt(vList.size)
        val selectedVariant = vList[targetIndex]
        val correspondingProduct = pList.find { it.id == selectedVariant.productId }
            ?: pList[0]

        // Calculate a simulated promotion drop
        val original = selectedVariant.originalPrice
        val discountFactor = Random.nextDouble(0.40, 0.65).toFloat() // 40% to 65% discount!
        val newPrice = (original * (1f - discountFactor)).toInt().toFloat()
        
        // Dynamic simulated Deal Score based on section 13 algorithms
        val discountPct = (((original - newPrice) / original) * 100).toInt()
        val finalDealScore = when {
            discountPct >= 45 -> Random.nextInt(92, 99) // exceptional
            discountPct >= 35 -> Random.nextInt(78, 89) // very good
            else -> Random.nextInt(62, 74) // good query
        }

        // Update active variant
        val updatedVariant = selectedVariant.copy(
            currentPrice = newPrice,
            dealScore = finalDealScore
        )
        dao.insertVariant(updatedVariant)

        // Write historical snapshot point representing today's newly scraped minimum
        val now = System.currentTimeMillis()
        dao.insertPriceSnapshot(
            PriceSnapshotEntity(
                variantId = selectedVariant.id,
                timestamp = now,
                price = newPrice
            )
        )

        // Update store offer corresponding to Notino (cheapest)
        val stores = listOf("Notino", "Douglas", "Sephora", "Flaconi", "Hebe")
        val randStore = stores.random()
        dao.insertStoreOffer(
            StoreOfferEntity(
                variantId = selectedVariant.id,
                storeName = randStore,
                price = newPrice,
                directLink = "https://www.${randStore.lowercase()}.pl"
            )
        )

        // Generate the Alert notification object
        val statusLabel = when (finalDealScore) {
            in 90..100 -> "WYJĄTKOWA OKAZJA"
            in 75..89 -> "BARDZO DOBRA OKAZJA"
            else -> "DOBRA PROMO"
        }

        val log = NotificationLog(
            title = "Nowy Skan: $statusLabel [$finalDealScore/100]!",
            body = "Zeskanowano sklep $randStore: zapach ${correspondingProduct.brand} ${correspondingProduct.name} (${selectedVariant.capacityMl}ml, ${selectedVariant.concentration}) przeceniony z ${original.toInt()} PLN do ${newPrice.toInt()} PLN! Rabat: $discountPct%.",
            variantId = selectedVariant.id,
            timestamp = now
        )
        dao.insertNotificationLog(log)
        return log
    }
}
