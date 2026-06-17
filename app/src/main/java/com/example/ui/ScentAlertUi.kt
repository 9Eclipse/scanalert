@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.database.BrandEntity
import com.example.database.NotificationLog
import com.example.database.PerfumeProduct
import com.example.database.ProductVariantEntity
import com.example.database.UserPreferencesEntity
import com.example.ui.theme.*
import com.example.viewmodel.ScentViewModel
import com.example.viewmodel.VariantDetailsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Screen Enumeration
enum class AppScreen {
    ONBOARDING,
    MAIN_HUB,
    DETAILS
}

// Bottom Navigation Tabs
enum class HubTab {
    FEED,     // Dla Ciebie
    EXPLORE,  // Szukaj i Top
    WATCHLIST,// Ulubione
    ALERTS,   // Alerty powiadomień
    SETTINGS  // Ustawienia
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScentAlertApp(
    viewModel: ScentViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(AppScreen.ONBOARDING) }
    var activeTab by remember { mutableStateOf(HubTab.FEED) }

    // Read general flows
    val dbBrands by viewModel.brands.collectAsStateWithLifecycle()
    val dbProducts by viewModel.perfumeProducts.collectAsStateWithLifecycle()
    val dbVariants by viewModel.productVariants.collectAsStateWithLifecycle()
    val dbPref by viewModel.userPreferences.collectAsStateWithLifecycle()
    val dbNotifications by viewModel.notifications.collectAsStateWithLifecycle()
    val watchedList by viewModel.watchedVariants.collectAsStateWithLifecycle()
    val detailsState by viewModel.variantDetailsState.collectAsStateWithLifecycle()

    // Scrapper process state UI indicators
    var isScrapingRunning by remember { mutableStateOf(false) }
    var recentScrapedItem by remember { mutableStateOf<NotificationLog?>(null) }
    var showOverlayNotification by remember { mutableStateOf(false) }

    // Seed checking to skip onboarding if brands favorite list has been defined previously
    LaunchedEffect(dbBrands) {
        if (dbBrands.any { it.isFavorite } && currentScreen == AppScreen.ONBOARDING) {
            currentScreen = AppScreen.MAIN_HUB
        }
    }

    // Dynamic banner disappearance
    LaunchedEffect(showOverlayNotification) {
        if (showOverlayNotification) {
            delay(5000)
            showOverlayNotification = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MatteCharcoal)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "screen_tr"
        ) { screen ->
            when (screen) {
                AppScreen.ONBOARDING -> {
                    OnboardingScreen(
                        brands = dbBrands,
                        onBrandToggle = { name, fav -> viewModel.toggleFavoriteBrand(name, fav) },
                        onFinishOnboarding = {
                            currentScreen = AppScreen.MAIN_HUB
                        }
                    )
                }

                AppScreen.MAIN_HUB -> {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MatteCharcoal,
                        bottomBar = {
                            NavigationBar(
                                containerColor = DarkVelvet,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = activeTab == HubTab.FEED,
                                    onClick = { activeTab = HubTab.FEED },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Dla Ciebie") },
                                    label = { Text("Dla Ciebie", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("tab_feed"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = RoseGold,
                                        indicatorColor = RoseGold,
                                        unselectedIconColor = NeutralMuted,
                                        unselectedTextColor = NeutralMuted
                                    )
                                )
                                NavigationBarItem(
                                    selected = activeTab == HubTab.EXPLORE,
                                    onClick = { activeTab = HubTab.EXPLORE },
                                    icon = { Icon(Icons.Default.Search, contentDescription = "Szukaj") },
                                    label = { Text("Odkrywaj", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("tab_explore"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = RoseGold,
                                        indicatorColor = RoseGold,
                                        unselectedIconColor = NeutralMuted,
                                        unselectedTextColor = NeutralMuted
                                    )
                                )
                                NavigationBarItem(
                                    selected = activeTab == HubTab.WATCHLIST,
                                    onClick = { activeTab = HubTab.WATCHLIST },
                                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Ulubione") },
                                    label = { Text("Ulubione", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("tab_watchlist"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = RoseGold,
                                        indicatorColor = RoseGold,
                                        unselectedIconColor = NeutralMuted,
                                        unselectedTextColor = NeutralMuted
                                    )
                                )
                                NavigationBarItem(
                                    selected = activeTab == HubTab.ALERTS,
                                    onClick = { activeTab = HubTab.ALERTS },
                                    icon = {
                                        Box {
                                            Icon(Icons.Default.Notifications, contentDescription = "Alerty")
                                            if (dbNotifications.any { !it.read }) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(ScentGood, CircleShape)
                                                        .align(Alignment.TopEnd)
                                                )
                                            }
                                        }
                                    },
                                    label = { Text("Alerty", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("tab_alerts"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = RoseGold,
                                        indicatorColor = RoseGold,
                                        unselectedIconColor = NeutralMuted,
                                        unselectedTextColor = NeutralMuted
                                    )
                                )
                                NavigationBarItem(
                                    selected = activeTab == HubTab.SETTINGS,
                                    onClick = { activeTab = HubTab.SETTINGS },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ustawienia") },
                                    label = { Text("Profil", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("tab_settings"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = RoseGold,
                                        indicatorColor = RoseGold,
                                        unselectedIconColor = NeutralMuted,
                                        unselectedTextColor = NeutralMuted
                                    )
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (activeTab) {
                                HubTab.FEED -> {
                                    FeedTab(
                                        variants = dbVariants,
                                        products = dbProducts,
                                        brands = dbBrands,
                                        preferences = dbPref,
                                        isScrapingRunning = isScrapingRunning,
                                        onScraperTrigger = {
                                            isScrapingRunning = true
                                            viewModel.runScraperSimulation { log ->
                                                recentScrapedItem = log
                                                isScrapingRunning = false
                                                showOverlayNotification = true
                                            }
                                        },
                                        onSelectProduct = { variantId ->
                                            viewModel.selectVariant(variantId)
                                            currentScreen = AppScreen.DETAILS
                                        },
                                        onToggleWatch = { id, watch ->
                                            viewModel.toggleWatchVariant(id, watch)
                                        }
                                    )
                                }

                                HubTab.EXPLORE -> {
                                    ExploreTab(
                                        variants = dbVariants,
                                        products = dbProducts,
                                        onSelectProduct = { variantId ->
                                            viewModel.selectVariant(variantId)
                                            currentScreen = AppScreen.DETAILS
                                        },
                                        onToggleWatch = { id, watch ->
                                            viewModel.toggleWatchVariant(id, watch)
                                        }
                                    )
                                }

                                HubTab.WATCHLIST -> {
                                    WatchlistTab(
                                        favoritesBrands = dbBrands,
                                        watchedVariants = watchedList,
                                        products = dbProducts,
                                        onSelectProduct = { variantId ->
                                            viewModel.selectVariant(variantId)
                                            currentScreen = AppScreen.DETAILS
                                        },
                                        onToggleBrand = { name, watch ->
                                            viewModel.toggleFavoriteBrand(name, watch)
                                        },
                                        onToggleWatch = { id, watch ->
                                            viewModel.toggleWatchVariant(id, watch)
                                        }
                                    )
                                }

                                HubTab.ALERTS -> {
                                    AlertsTab(
                                        notifications = dbNotifications,
                                        onSelectAlert = { variantId ->
                                            viewModel.selectVariant(variantId)
                                            currentScreen = AppScreen.DETAILS
                                        },
                                        onDeleteAlert = { id -> viewModel.deleteNotification(id) },
                                        onClearAll = { viewModel.clearAllNotifications() }
                                    )
                                }

                                HubTab.SETTINGS -> {
                                    SettingsTab(
                                        preferences = dbPref,
                                        onSavePrefs = { minDisc, testers, giftSets, quiet, maxP, cap ->
                                            viewModel.updatePreferences(minDisc, testers, giftSets, quiet, maxP, cap)
                                        },
                                        onResetOnboarding = {
                                            coroutineScope.launch {
                                                // Clear favorites to force onboarding screen again
                                                dbBrands.forEach {
                                                    viewModel.toggleFavoriteBrand(it.name, false)
                                                }
                                                currentScreen = AppScreen.ONBOARDING
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                AppScreen.DETAILS -> {
                    DetailsScreen(
                        state = detailsState,
                        onBack = { currentScreen = AppScreen.MAIN_HUB },
                        onToggleWatch = { id, watch ->
                            viewModel.toggleWatchVariant(id, watch)
                        }
                    )
                }
            }
        }

        // Native sliding overlay banner showing real-time Push alerts
        AnimatedVisibility(
            visible = showOverlayNotification,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .zIndex(100f)
        ) {
            recentScrapedItem?.let { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("overlay_toast_alert")
                        .border(1.dp, RoseGold, RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.selectVariant(log.variantId)
                            activeTab = HubTab.FEED
                            currentScreen = AppScreen.DETAILS
                            showOverlayNotification = false
                        },
                    colors = CardDefaults.cardColors(containerColor = DarkVelvet),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ScentExceptional.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = ScentExceptional)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RoseGold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = log.body,
                                style = MaterialTheme.typography.bodySmall.copy(color = NeutralCream),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            Icons.Default.Done,
                            contentDescription = "Zobacz",
                            tint = ScentVeryGood,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// 1. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(
    brands: List<BrandEntity>,
    onBrandToggle: (String, Boolean) -> Unit,
    onFinishOnboarding: () -> Unit
) {
    var minDiscount by remember { mutableStateOf(20f) }
    var agreementChecked by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SCENTALERT",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = RoseGold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 4.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Ekskluzywny monitoring promocji perfum",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Brand Selector
            Text(
                text = "Wybierz swoje ulubione marki (minimum 1):",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeutralCream
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-selection row style with chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                brands.forEach { brand ->
                    FilterChip(
                        selected = brand.isFavorite,
                        onClick = { onBrandToggle(brand.name, !brand.isFavorite) },
                        label = { Text(brand.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoseGold,
                            selectedLabelColor = Color.Black,
                            containerColor = LightVelvet,
                            labelColor = NeutralMuted
                        ),
                        modifier = Modifier.testTag("brand_chip_${brand.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Threshold Discount settings
            Text(
                text = "Minimalny pożądany rabat: ${minDiscount.toInt()}%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeutralCream
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = minDiscount,
                onValueChange = { minDiscount = it },
                valueRange = 10f..50f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = RoseGold,
                    activeTrackColor = RoseGold,
                    inactiveTrackColor = LightVelvet
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Agreement switch box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkVelvet, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreementChecked,
                    onCheckedChange = { agreementChecked = it },
                    colors = CheckboxDefaults.colors(checkedColor = RoseGold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Włącz natychmiastowe alerty push",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeutralCream
                    )
                    Text(
                        text = "Wyślemy powiadomienie tylko dla ofert z wysokim Deal Score",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMuted
                    )
                }
            }
        }

        val hasSelectedBrand = brands.any { it.isFavorite }

        Button(
            onClick = onFinishOnboarding,
            enabled = hasSelectedBrand,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("onboarding_done_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = RoseGold,
                contentColor = Color.Black,
                disabledContainerColor = LightVelvet,
                disabledContentColor = NeutralMuted
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (hasSelectedBrand) "PRZEJDŹ DO KORZYSTANIA" else "WYBIERZ PRZYNAJMNIEJ JEDNĄ MARKĘ",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

// 2. MAIN FEED "DLA CIEBIE"
@Composable
fun FeedTab(
    variants: List<ProductVariantEntity>,
    products: List<PerfumeProduct>,
    brands: List<BrandEntity>,
    preferences: UserPreferencesEntity?,
    isScrapingRunning: Boolean,
    onScraperTrigger: () -> Unit,
    onSelectProduct: (Int) -> Unit,
    onToggleWatch: (Int, Boolean) -> Unit
) {
    val favoriteBrands = brands.filter { it.isFavorite }.map { it.name.lowercase() }

    // Live filtration based on user onboarding choices & user settings
    val filteredDeals = remember(variants, products, favoriteBrands, preferences) {
        variants.filter { variant ->
            val correspondingProduct = products.find { it.id == variant.productId }
            val brandMatch = correspondingProduct == null || favoriteBrands.contains(correspondingProduct.brand.lowercase())
            val testerMatch = !variant.isTester || preferences?.excludeTesters == false
            val giftMatch = !variant.isGiftSet || preferences?.excludeGiftSets == false
            val discountMatch = variant.discountPercent >= (preferences?.minDiscountPercent ?: 20)
            val priceMatch = variant.currentPrice <= (preferences?.maxPrice ?: 1000)

            brandMatch && testerMatch && giftMatch && discountMatch && priceMatch
        }.sortedByDescending { it.dealScore }
    }

    var activeBrandFilter by remember { mutableStateOf("Wszystkie") }

    val filteredByBrandDeals = remember(filteredDeals, products, activeBrandFilter) {
        if (activeBrandFilter == "Wszystkie") {
            filteredDeals
        } else {
            filteredDeals.filter { variant ->
                val correspondingProduct = products.find { it.id == variant.productId }
                correspondingProduct?.brand?.lowercase() == activeBrandFilter.lowercase()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DLA CIEBIE",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = RoseGold,
                            fontFamily = FontFamily.Serif
                        )
                    )
                    Text(
                        text = "Najlepsze spersonalizowane oferty",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMuted
                    )
                }

                // Dynamic scrapper trigger action button
                Button(
                    onClick = onScraperTrigger,
                    enabled = !isScrapingRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightVelvet,
                        contentColor = RoseGold
                    ),
                    modifier = Modifier.testTag("scrapper_run_btn")
                ) {
                    if (isScrapingRunning) {
                        CircularProgressIndicator(
                            color = RoseGold,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Skan...", fontSize = 12.sp)
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Skanuj teraz",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Skanuj", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brand filters row chips
            val relevantBrands = listOf("Wszystkie") + brands.filter { it.isFavorite }.map { it.name }
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(relevantBrands) { bName ->
                    FilterChip(
                        selected = activeBrandFilter == bName,
                        onClick = { activeBrandFilter = bName },
                        label = { Text(bName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoseGold,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkVelvet,
                            labelColor = NeutralMuted
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (filteredByBrandDeals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = NeutralMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Brak promocji pasujących do kryteriów",
                            color = NeutralMuted,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Zmniejsz próg rabatu lub uruchom skanowanie sklepów",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMuted.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredByBrandDeals) { item ->
                val prod = products.find { it.id == item.productId }
                if (prod != null) {
                    DealCard(
                        variant = item,
                        product = prod,
                        onSelect = { onSelectProduct(item.id) },
                        onToggleWatch = { onToggleWatch(item.id, !item.isWatched) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

// 3. EXPLORE TAB (Search & Top rankings)
@Composable
fun ExploreTab(
    variants: List<ProductVariantEntity>,
    products: List<PerfumeProduct>,
    onSelectProduct: (Int) -> Unit,
    onToggleWatch: (Int, Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeRankSelected by remember { mutableStateOf("TOP DEAL SCORE") }

    val searchedStoreItems = remember(variants, products, searchQuery) {
        if (searchQuery.isBlank()) {
            variants
        } else {
            variants.filter { variant ->
                val prod = products.find { it.id == variant.productId }
                prod?.name?.lowercase()?.contains(searchQuery.lowercase()) == true ||
                        prod?.brand?.lowercase()?.contains(searchQuery.lowercase()) == true ||
                        variant.concentration.lowercase().contains(searchQuery.lowercase())
            }
        }
    }

    val rankedItems = remember(searchedStoreItems, activeRankSelected) {
        when (activeRankSelected) {
            "TOP DEAL SCORE" -> searchedStoreItems.sortedByDescending { it.dealScore }
            "NAJTAŃSZE (1 ML)" -> searchedStoreItems.sortedBy { it.pricePerMl }
            "NAJWIĘKSZY % RABATU" -> searchedStoreItems.sortedByDescending { it.discountPercent }
            else -> searchedStoreItems
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ODKRYWAJ OKAZJE",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = RoseGold,
                fontFamily = FontFamily.Serif
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search text box
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Wyszukaj zapach, markę...", color = NeutralMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Wyszukaj", tint = NeutralMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_text_input")
                .clip(RoundedCornerShape(8.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DarkVelvet,
                unfocusedContainerColor = DarkVelvet,
                focusedTextColor = NeutralCream,
                unfocusedTextColor = NeutralCream,
                cursorColor = RoseGold,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ranking tabs selectors
        val ranks = listOf("TOP DEAL SCORE", "NAJTAŃSZE (1 ML)", "NAJWIĘKSZY % RABATU")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ranks) { rank ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (activeRankSelected == rank) RoseGold else DarkVelvet,
                    modifier = Modifier.clickable { activeRankSelected = rank }
                ) {
                    Text(
                        text = rank,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (activeRankSelected == rank) Color.Black else NeutralMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (rankedItems.isEmpty()) {
                item {
                    Text(
                        text = "Brak wyników wyszukiwania",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        textAlign = TextAlign.Center,
                        color = NeutralMuted
                    )
                }
            } else {
                items(rankedItems) { item ->
                    val prod = products.find { it.id == item.productId }
                    if (prod != null) {
                        DealCard(
                            variant = item,
                            product = prod,
                            onSelect = { onSelectProduct(item.id) },
                            onToggleWatch = { onToggleWatch(item.id, !item.isWatched) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

// 4. WATCHLIST TAB (Favorites)
@Composable
fun WatchlistTab(
    favoritesBrands: List<BrandEntity>,
    watchedVariants: List<ProductVariantEntity>,
    products: List<PerfumeProduct>,
    onSelectProduct: (Int) -> Unit,
    onToggleBrand: (String, Boolean) -> Unit,
    onToggleWatch: (Int, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "OBSERWOWANE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = RoseGold,
                    fontFamily = FontFamily.Serif
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Twoje ulubione marki:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = NeutralCream
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                favoritesBrands.forEach { b ->
                    FilterChip(
                        selected = b.isFavorite,
                        onClick = { onToggleBrand(b.name, !b.isFavorite) },
                        label = { Text(b.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoseGold,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkVelvet,
                            labelColor = NeutralMuted
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Obserwowane flakony i okazje:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = NeutralCream
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (watchedVariants.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Watchlist pusta",
                            tint = NeutralMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Lista obserwowanych flakonów jest pusta",
                            color = NeutralMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(watchedVariants) { item ->
                val prod = products.find { it.id == item.productId }
                if (prod != null) {
                    DealCard(
                        variant = item,
                        product = prod,
                        onSelect = { onSelectProduct(item.id) },
                        onToggleWatch = { onToggleWatch(item.id, !item.isWatched) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

// 5. ALERTS BULLETIN BOARD TAB (FCM mockup log console)
@Composable
fun AlertsTab(
    notifications: List<NotificationLog>,
    onSelectAlert: (Int) -> Unit,
    onDeleteAlert: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CENTRUM ALERTÓW",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = RoseGold,
                        fontFamily = FontFamily.Serif
                    )
                )
                Text(
                    text = "Logi natychmiastowych alertów z parserów",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMuted
                )
            }

            if (notifications.isNotEmpty()) {
                IconButton(
                    onClick = onClearAll,
                    modifier = Modifier.testTag("clear_alerts_btn")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Wyczyść wszystko",
                        tint = ScentRegular
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (notifications.isEmpty()) {
                item {
                    Text(
                        text = "Historyczna lista alertów jest pusta.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        textAlign = TextAlign.Center,
                        color = NeutralMuted
                    )
                }
            } else {
                items(notifications) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("alert_item_${log.id}")
                            .clickable { onSelectAlert(log.variantId) },
                        colors = CardDefaults.cardColors(containerColor = DarkVelvet)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = RoseGold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = RoseGold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = log.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralCream
                                )
                            }
                            IconButton(onClick = { onDeleteAlert(log.id) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Usuń zapowiedź",
                                    tint = ScentRegular,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. SETTINGS & PREFERENCES EDIT TAB
@Composable
fun SettingsTab(
    preferences: UserPreferencesEntity?,
    onSavePrefs: (Int, Boolean, Boolean, Boolean, Int, String) -> Unit,
    onResetOnboarding: () -> Unit
) {
    val nonNullPrefs = preferences ?: UserPreferencesEntity()

    var minDiscount by remember(nonNullPrefs) { mutableStateOf(nonNullPrefs.minDiscountPercent.toFloat()) }
    var excludeTesters by remember(nonNullPrefs) { mutableStateOf(nonNullPrefs.excludeTesters) }
    var excludeGiftSets by remember(nonNullPrefs) { mutableStateOf(nonNullPrefs.excludeGiftSets) }
    var enableQuietHours by remember(nonNullPrefs) { mutableStateOf(nonNullPrefs.enableQuietHours) }
    var maxPriceLimit by remember(nonNullPrefs) { mutableStateOf(nonNullPrefs.maxPrice.toFloat()) }
    var preferredCaps by remember(nonNullPrefs) { mutableStateOf(nonNullPrefs.preferredCapacities) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "USTAWIENIA PROFILU",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = RoseGold,
                    fontFamily = FontFamily.Serif
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Zarządzaj inteligentnym filtrem spamu i cichym czasem powiadomień",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMuted
            )
        }

        // Section: Thresholds
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkVelvet),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Inteligentne Filtrowanie",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = RoseGold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Minimalny pożądany rabat: ${minDiscount.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralCream
                    )
                    Slider(
                        value = minDiscount,
                        onValueChange = { minDiscount = it },
                        valueRange = 10f..50f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = RoseGold,
                            activeTrackColor = RoseGold,
                            inactiveTrackColor = LightVelvet
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Maksymalna cena za flakon: ${maxPriceLimit.toInt()} PLN",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralCream
                    )
                    Slider(
                        value = maxPriceLimit,
                        onValueChange = { maxPriceLimit = it },
                        valueRange = 200f..1500f,
                        steps = 13,
                        colors = SliderDefaults.colors(
                            thumbColor = RoseGold,
                            activeTrackColor = RoseGold,
                            inactiveTrackColor = LightVelvet
                        )
                    )
                }
            }
        }

        // Section: Exclusions switches
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkVelvet),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Wykluczenia i formaty",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = RoseGold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Wyklucz flakony typu Tester", color = NeutralCream)
                            Text("Ukryj tanie opakowania testowe z podglądu", style = MaterialTheme.typography.bodySmall, color = NeutralMuted)
                        }
                        Switch(
                            checked = excludeTesters,
                            onCheckedChange = { excludeTesters = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = RoseGold, checkedTrackColor = GoldAccent)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Wyklucz Zestawy (Giftsety)", color = NeutralCream)
                            Text("Pokazuj wyłącznie standardowe butelki", style = MaterialTheme.typography.bodySmall, color = NeutralMuted)
                        }
                        Switch(
                            checked = excludeGiftSets,
                            onCheckedChange = { excludeGiftSets = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = RoseGold, checkedTrackColor = GoldAccent)
                        )
                    }
                }
            }
        }

        // Section: Quiet hours
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkVelvet),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tryb nocny / Quiet Hours",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = RoseGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Blokuj powiadomienia natychmiastowe w godzinach nocnych (22:00 do 07:00)",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cisza nocna aktywna", color = NeutralCream)
                        Switch(
                            checked = enableQuietHours,
                            onCheckedChange = { enableQuietHours = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = RoseGold, checkedTrackColor = GoldAccent)
                        )
                    }
                }
            }
        }

        // Save Button Row
        item {
            Button(
                onClick = {
                    onSavePrefs(
                        minDiscount.toInt(),
                        excludeTesters,
                        excludeGiftSets,
                        enableQuietHours,
                        maxPriceLimit.toInt(),
                        preferredCaps
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoseGold, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_btn"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ZAPISZ PREFERENCJE FILTRÓW", fontWeight = FontWeight.Bold)
            }
        }

        // Developer tools Section (Re-trigger onboarding for testing)
        item {
            Divider(color = LightVelvet)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onResetOnboarding,
                border = BorderStroke(1.dp, ScentRegular),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("PONÓW CONFIG STARTOWY (ONBOARDING)", fontSize = 12.sp, color = NeutralMuted)
            }
        }
    }
}

// 7. DETAIL VIEW COMPOSABLE SHEET
@Composable
fun DetailsScreen(
    state: VariantDetailsState,
    onBack: () -> Unit,
    onToggleWatch: (Int, Boolean) -> Unit
) {
    val varObject = state.variant
    val prodObject = state.product

    if (varObject == null || prodObject == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RoseGold)
        }
        return
    }

    var showConfirmationShopModal by remember { mutableStateOf(false) }
    var selectedShopTarget by remember { mutableStateOf("") }
    var aiResponse by remember { mutableStateOf("") }
    var isLoadingAi by remember { mutableStateOf(false) }
    var userQuestion by remember { mutableStateOf("") }
    val detailsCoroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("details_layout"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // App header inside detail
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_button_details")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz", tint = RoseGold)
                }

                Text(
                    text = "OCENA SPECYFIKACJI",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = RoseGold,
                        letterSpacing = 1.sp
                    )
                )

                IconButton(
                    onClick = { onToggleWatch(varObject.id, !varObject.isWatched) },
                    modifier = Modifier.testTag("watch_toggle_details")
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Obserwuj",
                        tint = if (varObject.isWatched) ScentExceptional else NeutralMuted
                    )
                }
            }
        }

        // Scent header illustration card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PerfumeIllustrationSmall(brand = prodObject.brand, modifier = Modifier.size(100.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = prodObject.brand.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RoseGold,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = prodObject.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = NeutralCream,
                            fontFamily = FontFamily.Serif
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${varObject.capacityMl} ml  •  ${varObject.concentration}",
                        color = NeutralMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Deal Score circle dial and original margin details
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DealScoreGauge(score = varObject.dealScore)

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    MetricWidget(
                        label = "NAJTAŃSZA OFERTA",
                        valStr = "${varObject.currentPrice.toInt()} PLN",
                        colorVal = ScentVeryGood
                    )
                    MetricWidget(
                        label = "CENA RYNKOWA",
                        valStr = "${varObject.originalPrice.toInt()} PLN",
                        colorVal = NeutralMuted
                    )
                    MetricWidget(
                        label = "RABAT PROCENTOWY",
                        valStr = "-${varObject.discountPercent}%",
                        colorVal = ScentExceptional
                    )
                    MetricWidget(
                        label = "CENA ZA 1 ML",
                        valStr = String.format("%.2f PLN", varObject.pricePerMl),
                        colorVal = NeutralCream
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Custom canvas graph visualization showing price history
        item {
            val graphPoints = state.snapshots.map { it.price }
            val pricesToShow: List<Float> = if (graphPoints.isNotEmpty()) {
                graphPoints
            } else {
                listOf<Float>((varObject.originalPrice * 0.95f), varObject.currentPrice)
            }
            PriceHistoryGraph(
                prices = pricesToShow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Competitor listing table Section 11/4.1
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "PORÓWNANIE SKLEPÓW KOMPETYCYJNYCH",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RoseGold,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                for ((index, offer) in state.offers.withIndex()) {
                    val isCheapest = index == 0
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCheapest) ScentVeryGood.copy(alpha = 0.08f) else DarkVelvet,
                        border = if (isCheapest) BorderStroke(1.dp, ScentVeryGood.copy(alpha = 0.5f)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isCheapest) ScentVeryGood else LightVelvet,
                                    modifier = Modifier.size(10.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = offer.storeName,
                                        fontWeight = FontWeight.Bold,
                                        color = NeutralCream
                                    )
                                    if (isCheapest) {
                                        Text(
                                            text = "NAJTAŃSZY PARTNER",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ScentVeryGood
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${offer.price.toInt()} PLN",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCheapest) ScentVeryGood else NeutralCream,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Button(
                                    onClick = {
                                        selectedShopTarget = offer.storeName
                                        showConfirmationShopModal = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCheapest) ScentVeryGood else LightVelvet,
                                        contentColor = if (isCheapest) Color.Black else RoseGold
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Idź", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // AI Doradca Zapachowy - Gemini AI Integration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkVelvet),
                border = BorderStroke(1.dp, RoseGold.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("ai_advisor_card"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✨ INTELIGENTNY DORADCA AI",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = RoseGold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (isLoadingAi) {
                            CircularProgressIndicator(
                                color = RoseGold,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Twój osobisty asystent premium po świecie zapachów. Poznaj unikalne szczegóły o tym flakonie od Gemini AI:",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralCream.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons for preset questions
                    Text(
                        text = "Szybkie tematy analizy:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeutralMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                if (!isLoadingAi) {
                                    isLoadingAi = true
                                    aiResponse = "Analizuję profil zapachowy..."
                                    detailsCoroutineScope.launch {
                                        aiResponse = GeminiClient.askGemini(
                                            "Opisz szczegółowo profil zapachowy perfum ${prodObject.brand} ${prodObject.name} (${varObject.concentration}). " +
                                            "Wyjaśnij ich nuty zapachowe (głowy, serca i bazy) w elegancki i zwięzły sposób. Odpowiedz po polsku."
                                        )
                                        isLoadingAi = false
                                    }
                                }
                            },
                            label = { Text("Profil zapachowy (nuty)", fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                labelColor = RoseGold,
                                containerColor = LightVelvet
                            )
                        )

                        SuggestionChip(
                            onClick = {
                                if (!isLoadingAi) {
                                    isLoadingAi = true
                                    aiResponse = "Analizuję parametry i okazje..."
                                    detailsCoroutineScope.launch {
                                        aiResponse = GeminiClient.askGemini(
                                            "Dla kogo i na jaką okazję oraz porę roku najbardziej pasuje zapach ${prodObject.brand} ${prodObject.name}? " +
                                            "Zanalizuj również jego trwałość i projekcję (sillage). Odpowiedz po polsku zwięźle."
                                        )
                                        isLoadingAi = false
                                    }
                                }
                            },
                            label = { Text("Okazje i parametry", fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                labelColor = RoseGold,
                                containerColor = LightVelvet
                            )
                        )

                        SuggestionChip(
                            onClick = {
                                if (!isLoadingAi) {
                                    isLoadingAi = true
                                    aiResponse = "Generuję wskazówki warstwowania..."
                                    detailsCoroutineScope.launch {
                                        aiResponse = GeminiClient.askGemini(
                                            "Z jakimi innymi typami aromatów lub nut zapachowych można łączyć (layerować) zapach ${prodObject.brand} ${prodObject.name}? " +
                                            "Podaj 2 praktyczne sugestie luksusowego łączenia zapachów dla marki ${prodObject.brand}. Odpowiedz po polsku."
                                        )
                                        isLoadingAi = false
                                    }
                                }
                            },
                            label = { Text("Z czym łączyć (layering)?", fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                labelColor = RoseGold,
                                containerColor = LightVelvet
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // AI Response container block
                    if (aiResponse.isNotEmpty()) {
                        Surface(
                            color = LightVelvet,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Odpowiedź doradcy AI:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = RoseGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = aiResponse,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NeutralCream,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Custom free-form questions input row
                    TextField(
                        value = userQuestion,
                        onValueChange = { userQuestion = it },
                        placeholder = { Text("Zadaj własne pytanie doradcy AI...", color = NeutralMuted, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (userQuestion.isNotBlank() && !isLoadingAi) {
                                        val prompt = "Użytkownik pyta o perfumy ${prodObject.brand} ${prodObject.name} (${varObject.concentration}): \"$userQuestion\". " +
                                                "Odpowiedz jako profesjonalny, luksusowy ekspert perfumeryjny (po polsku, zwięźle i merytorycznie)."
                                        isLoadingAi = true
                                        aiResponse = "Generuję odpowiedź dla Ciebie..."
                                        detailsCoroutineScope.launch {
                                            aiResponse = GeminiClient.askGemini(prompt)
                                            isLoadingAi = false
                                        }
                                        userQuestion = ""
                                    }
                                },
                                enabled = userQuestion.isNotBlank() && !isLoadingAi
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Wyślij",
                                    tint = if (userQuestion.isNotBlank() && !isLoadingAi) RoseGold else NeutralMuted
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = LightVelvet,
                            unfocusedContainerColor = LightVelvet,
                            focusedTextColor = NeutralCream,
                            unfocusedTextColor = NeutralCream,
                            cursorColor = RoseGold,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }
        }
    }

    // In-app checkout modal simulator (no dead-ends guarantee)
    if (showConfirmationShopModal) {
        AlertDialog(
            onDismissRequest = { showConfirmationShopModal = false },
            title = { Text("Kliknięto link partnerski", color = RoseGold) },
            text = {
                Text(
                    text = "System ScentAlert przekierowuje Cię bezpośrednio do sklepu partnerskiego $selectedShopTarget. Twój kod promocyjny i afiliacja zostały pomyślnie zaaplikowane w tle w przeglądarce.",
                    color = NeutralCream
                )
            },
            confirmButton = {
                Button(
                    onClick = { showConfirmationShopModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseGold, contentColor = Color.Black)
                ) {
                    Text("OK, PRZEIDŹ DO SKLEPU")
                }
            },
            containerColor = DarkVelvet
        )
    }
}

@Composable
fun PerfumeIllustrationSmall(
    brand: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(DarkVelvet, RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        PerfumeBottleIllustration(brand = brand, modifier = Modifier.size(50.dp, 60.dp))
    }
}

@Composable
fun MetricWidget(
    label: String,
    valStr: String,
    colorVal: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = NeutralMuted, letterSpacing = 1.sp)
        )
        Text(
            text = valStr,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = colorVal
            )
        )
    }
}

// 8. DEALS CARD RENDER LISTING
@Composable
fun DealCard(
    variant: ProductVariantEntity,
    product: PerfumeProduct,
    onSelect: () -> Unit,
    onToggleWatch: () -> Unit
) {
    val tierColor = when (variant.dealScore) {
        in 90..100 -> ScentExceptional
        in 75..89 -> ScentVeryGood
        in 60..74 -> ScentGood
        else -> ScentRegular
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("deal_card_${variant.id}")
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = DarkVelvet),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left custom rendered perfume bottle
            PerfumeIllustrationSmall(brand = product.brand, modifier = Modifier.size(70.dp, 80.dp))

            Spacer(modifier = Modifier.width(12.dp))

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.brand.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RoseGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (variant.isTester) {
                        Surface(
                            color = ScentRegular.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                "TESTER",
                                color = NeutralMuted,
                                fontSize = 8.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeutralCream,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${variant.capacityMl}ml  •  ${variant.concentration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${variant.currentPrice.toInt()} PLN",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = ScentVeryGood
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${variant.originalPrice.toInt()} PLN",
                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                        color = NeutralMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = ScentExceptional.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "-${variant.discountPercent}%",
                            color = ScentExceptional,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Right watch/deal score display indices
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(80.dp)
            ) {
                IconButton(
                    onClick = onToggleWatch,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Zapisany flakon",
                        tint = if (variant.isWatched) ScentExceptional else NeutralMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tierColor
                ) {
                    Text(
                        text = "${variant.dealScore}",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// FlowRow wrapper utility to support non-breaking wrapping chips prior to JVM targets configurations
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}
