package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class VariantDetailsState(
    val variant: ProductVariantEntity? = null,
    val product: PerfumeProduct? = null,
    val offers: List<StoreOfferEntity> = emptyList(),
    val snapshots: List<PriceSnapshotEntity> = emptyList()
)

class ScentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DatabaseProvider.getRepository(application)

    // Exposed DB Flows
    val brands = repository.brands.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val perfumeProducts = repository.perfumeProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val productVariants = repository.productVariants.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val watchedVariants = repository.watchedVariants.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userPreferences = repository.userPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferencesEntity()
    )

    val notifications = repository.notifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected product variant ID for the dynamic Details screen
    private val _selectedVariantId = MutableStateFlow<Int?>(null)
    val selectedVariantId: StateFlow<Int?> = _selectedVariantId.asStateFlow()

    // Combining multiple database flows to build a completely reactive Details screen data package
    val variantDetailsState: StateFlow<VariantDetailsState> = _selectedVariantId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(VariantDetailsState())
            } else {
                combine(
                    productVariants,
                    perfumeProducts,
                    repository.getStoreOffersForVariant(id),
                    repository.getPriceSnapshotsForVariant(id)
                ) { variants, products, offers, snapshots ->
                    val variant = variants.find { it.id == id }
                    val product = products.find { it.id == variant?.productId }
                    VariantDetailsState(
                        variant = variant,
                        product = product,
                        offers = offers.sortedBy { it.price },
                        snapshots = snapshots
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VariantDetailsState()
        )

    init {
        // Pre-seed premium perfume details so the user instantly has high-fidelity data
        viewModelScope.launch {
            repository.preseedDatabaseIfEmpty()
        }
    }

    // Interactive Action triggers
    fun selectVariant(variantId: Int?) {
        _selectedVariantId.value = variantId
    }

    fun toggleFavoriteBrand(brandName: String, isNowFavorite: Boolean) {
        viewModelScope.launch {
            repository.updateFavoriteBrand(brandName, isNowFavorite)
        }
    }

    fun toggleWatchVariant(variantId: Int, isNowWatched: Boolean) {
        viewModelScope.launch {
            repository.updateWatchVariant(variantId, isNowWatched)
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun updatePreferences(
        minDiscount: Int,
        excludeTesters: Boolean,
        excludeGiftSets: Boolean,
        enableQuietHours: Boolean,
        maxPrice: Int,
        preferredCapacities: String
    ) {
        viewModelScope.launch {
            val updated = UserPreferencesEntity(
                id = 1,
                minDiscountPercent = minDiscount,
                excludeTesters = excludeTesters,
                excludeGiftSets = excludeGiftSets,
                enableQuietHours = enableQuietHours,
                maxPrice = maxPrice,
                preferredCapacities = preferredCapacities
            )
            repository.savePreferences(updated)
        }
    }

    // Simulated background Scraping thread. Instantly updates the database in real-time,
    // which triggers UI recompositions & appends a push alert log!
    fun runScraperSimulation(onResult: (NotificationLog) -> Unit) {
        viewModelScope.launch {
            try {
                val log = repository.simulateScraping()
                onResult(log)
            } catch (e: Exception) {
                // Ignore empty db errors during preseed delay
            }
        }
    }
}
