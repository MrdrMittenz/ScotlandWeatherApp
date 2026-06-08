package com.scotlandweather.app

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scotlandweather.app.data.model.ScotlandLocation
import com.scotlandweather.app.data.model.TileLayer
import com.scotlandweather.app.data.update.UpdateChecker
import com.scotlandweather.app.data.update.UpdateResult
import com.scotlandweather.app.ui.components.*
import com.scotlandweather.app.ui.screen.MapScreen
import com.scotlandweather.app.ui.theme.*
import com.scotlandweather.app.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScotlandWeatherApp(viewModel: WeatherViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    // UI state
    var showLocationList by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showFishingReports by remember { mutableStateOf(false) }
    var selectedLocationName by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Update check state
    var updateResult by remember { mutableStateOf<UpdateResult?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }

    // Get selected location data
    val selectedLocation = selectedLocationName?.let {
        viewModel.locationMap[it] ?: state.customLocations[it]
    }
    val selectedWeather = selectedLocationName?.let { viewModel.getWeatherForLocation(it) }
    val selectedSolunar = selectedLocationName?.let { viewModel.getSolunarForLocation(it) }
    val selectedRanking = selectedLocationName?.let { viewModel.getRankingForLocation(it) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    BoxWithConstraints {
        val isLandscape = maxWidth > maxHeight
        val onOpenSearch: () -> Unit = { showLocationList = true }
        val onShowFishingReports: () -> Unit = {
            viewModel.fetchCatchReports()
            showFishingReports = true
        }

        if (isLandscape) {
            LandscapeLayout(
                state = state,
                viewModel = viewModel,
                snackbarHostState = snackbarHostState,
                onOpenSearch = onOpenSearch,
                onLocationSelected = { name ->
                    selectedLocationName = name
                    showBottomSheet = true
                },
                onShowFishingReports = onShowFishingReports,
                isCheckingUpdate = isCheckingUpdate,
                onCheckUpdate = {
                    isCheckingUpdate = true
                    scope.launch {
                        updateResult = UpdateChecker.check(context)
                        isCheckingUpdate = false
                    }
                }
            )
        } else {
            PortraitLayout(
                state = state,
                viewModel = viewModel,
                snackbarHostState = snackbarHostState,
                onOpenSearch = onOpenSearch,
                onLocationSelected = { name ->
                    selectedLocationName = name
                    showBottomSheet = true
                },
                onShowFishingReports = onShowFishingReports,
                isCheckingUpdate = isCheckingUpdate,
                onCheckUpdate = {
                    isCheckingUpdate = true
                    scope.launch {
                        updateResult = UpdateChecker.check(context)
                        isCheckingUpdate = false
                    }
                }
            )
        }
    }

    // Search/location dialog
    if (showLocationList) {
        LocationSearchDialog(
            localLocations = viewModel.allLocations,
            searchResults = state.searchResults,
            isSearching = state.isSearching,
            isRefreshing = state.isRefreshing,
            searchQuery = searchQuery,
            onQueryChange = { q ->
                searchQuery = q
                if (q.length >= 3) viewModel.searchLocations(q)
                else viewModel.clearSearch()
            },
            onLocationSelected = { location ->
                selectedLocationName = location.name
                if (location.isCustom) {
                    viewModel.selectCustomLocation(location)
                } else {
                    viewModel.selectLocation(location)
                }
                showLocationList = false
                showBottomSheet = true
                searchQuery = ""
            },
            onDismiss = {
                showLocationList = false
                searchQuery = ""
                viewModel.clearSearch()
            }
        )
    }

    // Bottom sheet
    if (showBottomSheet && selectedLocation != null) {
        WeatherBottomSheet(
            selectedLocation = selectedLocation,
            weather = selectedWeather,
            solunar = selectedSolunar,
            ranking = selectedRanking,
            day = state.selectedDay,
            hour = state.selectedHour,
            isFavorite = state.favoriteLocations.contains(selectedLocation.name),
            onToggleFavorite = { viewModel.toggleFavorite(selectedLocation.name) },
            onDismiss = { showBottomSheet = false }
        )
    }

    // Fishing reports sheet
    if (showFishingReports) {
        FishingReportsSheet(
            catchReports = state.catchReports,
            activeSpecies = viewModel.getActiveSpecies(selectedWeather),
            isLoading = state.isLoadingReports,
            selectedLocation = selectedLocation,
            onRefresh = { viewModel.fetchCatchReports() },
            onDismiss = { showFishingReports = false }
        )
    }

    // Update check error
    LaunchedEffect(updateResult) {
        val result = updateResult ?: return@LaunchedEffect
        if (result.error != null) {
            snackbarHostState.showSnackbar(result.error)
            updateResult = null
        }
    }

    // Update available dialog
    updateResult?.let { result ->
        if (result.isAvailable && result.manifest != null) {
            AlertDialog(
                onDismissRequest = { updateResult = null },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text("Update Available", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text("Version ${result.manifest.versionName} is available.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.manifest.changelog,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                confirmButton = {
                    if (isDownloading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        TextButton(onClick = {
                            isDownloading = true
                            scope.launch {
                                val err = UpdateChecker.downloadAndInstall(context, result.manifest)
                                isDownloading = false
                                updateResult = null
                                if (err != null) {
                                    snackbarHostState.showSnackbar("Update failed: $err")
                                }
                            }
                        }) {
                            Text("Update")
                        }
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { updateResult = null }) {
                        Text("Later")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitLayout(
    state: com.scotlandweather.app.data.model.MapState,
    viewModel: WeatherViewModel,
    snackbarHostState: SnackbarHostState,
    onOpenSearch: () -> Unit,
    onLocationSelected: (String) -> Unit,
    onShowFishingReports: () -> Unit,
    isCheckingUpdate: Boolean,
    onCheckUpdate: () -> Unit
) {
    var showLayerMenu by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val barBrush = if (state.isDarkMode) AppBarGradient else AppBarGradientLight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(barBrush)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Fair Weather Fishing",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Scotland Fishing Planner",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onOpenSearch) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search locations",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { viewModel.toggleDarkMode() }) {
                            Icon(
                                imageVector = if (state.isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Toggle theme",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = {
                            if (!isCheckingUpdate) {
                                viewModel.loadAllData()
                            }
                        }) {
                            if (state.isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Color.White
                                )
                            }
                        }
                        IconButton(onClick = onCheckUpdate) {
                            if (isCheckingUpdate) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.GetApp,
                                    contentDescription = "Check for updates",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Main map
            MapScreen(
                state = state,
                onLocationClick = onLocationSelected
            )

            // Overlay controls (scrollable for landscape)
            val glassBg = glassBackground(state.isDarkMode)
            val glassBd = glassBorder(state.isDarkMode)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                color = glassBg,
                tonalElevation = 0.dp,
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, glassBd)
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LayerToggle(
                        selectedLayer = state.selectedLayer,
                        onLayerSelected = { layer ->
                            viewModel.selectLayer(layer)
                            showLayerMenu = false
                        },
                        expanded = showLayerMenu,
                        onToggle = { showLayerMenu = !showLayerMenu }
                    )

                    SmallFloatingActionButton(
                        onClick = { viewModel.loadAllData() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    SmallFloatingActionButton(
                        onClick = onShowFishingReports,
                        containerColor = OceanTeal,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Fishing Reports",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Debug: location count
            Text(
                text = "Weather: ${state.hourlyData.size}/${viewModel.allLocations.size}",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color(0xAA000000), RoundedCornerShape(4.dp))
                    .padding(4.dp)
            )

            // Bottom controls
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp)
                    .heightIn(max = 320.dp),
                shape = RoundedCornerShape(16.dp),
                color = glassBg,
                tonalElevation = 0.dp,
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, glassBd)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Legend(
                            layer = state.selectedLayer,
                            modifier = Modifier.width(160.dp)
                        )
                        if (state.favoriteLocations.isNotEmpty()) {
                            FavoritesBar(
                                favoriteNames = state.favoriteLocations,
                                onFavoriteClick = onLocationSelected,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    TimeSlider(
                        selectedDay = state.selectedDay,
                        selectedHour = state.selectedHour,
                        onDaySelected = { viewModel.selectDay(it) },
                        onHourSelected = { viewModel.selectHour(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandscapeLayout(
    state: com.scotlandweather.app.data.model.MapState,
    viewModel: WeatherViewModel,
    snackbarHostState: SnackbarHostState,
    onOpenSearch: () -> Unit,
    onLocationSelected: (String) -> Unit,
    onShowFishingReports: () -> Unit,
    isCheckingUpdate: Boolean,
    onCheckUpdate: () -> Unit
) {
    var showLayerMenu by remember { mutableStateOf(false) }
    val glassBg = glassBackground(state.isDarkMode)
    val glassBd = glassBorder(state.isDarkMode)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val barBrush = if (state.isDarkMode) AppBarGradient else AppBarGradientLight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(barBrush)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Fair Weather Fishing",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    actions = {
                        IconButton(onClick = onOpenSearch) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search locations",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { viewModel.toggleDarkMode() }) {
                            Icon(
                                imageVector = if (state.isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Toggle theme",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = {
                            if (!isCheckingUpdate) {
                                viewModel.loadAllData()
                            }
                        }) {
                            if (state.isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Color.White
                                )
                            }
                        }
                        IconButton(onClick = onCheckUpdate) {
                            if (isCheckingUpdate) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.GetApp,
                                    contentDescription = "Check for updates",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Map fills remaining vertical space
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                MapScreen(
                    state = state,
                    onLocationClick = onLocationSelected
                )

                // Debug: location count
                Text(
                    text = "Weather: ${state.hourlyData.size}/${viewModel.allLocations.size}",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .background(Color(0xAA000000), RoundedCornerShape(4.dp))
                        .padding(4.dp)
                )
            }

            // Compact control bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                color = glassBg,
                tonalElevation = 0.dp,
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, glassBd)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        SmallFloatingActionButton(
                            onClick = { showLayerMenu = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = layerToggleIcon(state.selectedLayer),
                                contentDescription = "Select layer",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showLayerMenu,
                            onDismissRequest = { showLayerMenu = false }
                        ) {
                            TileLayer.entries.forEach { layer ->
                                DropdownMenuItem(
                                    text = { Text(layer.displayName, fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.selectLayer(layer)
                                        showLayerMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = layerToggleIcon(layer),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    SmallFloatingActionButton(
                        onClick = { viewModel.loadAllData() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    SmallFloatingActionButton(
                        onClick = onShowFishingReports,
                        containerColor = OceanTeal,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Fishing Reports",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Legend(
                        layer = state.selectedLayer,
                        modifier = Modifier.width(120.dp)
                    )

                    if (state.favoriteLocations.isNotEmpty()) {
                        FavoritesBar(
                            favoriteNames = state.favoriteLocations,
                            onFavoriteClick = onLocationSelected,
                            modifier = Modifier.widthIn(max = 160.dp)
                        )
                    }
                }
            }

            // Compact TimeSlider (single row, no day buttons)
            Surface(
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(0.dp),
                color = glassBg.copy(alpha = 0.7f),
                tonalElevation = 0.dp,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, glassBd)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Day chips
                    val dayLabels = (0..2).map { offset ->
                        val date = java.time.LocalDate.now().plusDays(offset.toLong())
                        when (offset) { 0 -> "Tdy"; 1 -> "Tmrw"; else -> date.format(java.time.format.DateTimeFormatter.ofPattern("d/M")) }
                    }
                    dayLabels.forEachIndexed { index, label ->
                        val isDaySel = index == state.selectedDay
                        Surface(
                            onClick = { viewModel.selectDay(index) },
                            shape = RoundedCornerShape(4.dp),
                            color = if (isDaySel) OceanBlue else Color.Transparent,
                            border = if (isDaySel) null else androidx.compose.foundation.BorderStroke(1.dp, OceanBlue.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (isDaySel) Color.White else OceanBlue,
                                fontWeight = if (isDaySel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Hour slider
                    Slider(
                        value = state.selectedHour.toFloat() / 23f,
                        onValueChange = { viewModel.selectHour((it * 23f).toInt().coerceIn(0, 23)) },
                        modifier = Modifier.weight(1f).height(24.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = OceanBlue,
                            activeTrackColor = OceanBlue,
                            inactiveTrackColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Current hour
                    Text(
                        text = "${state.selectedHour.toString().padStart(2, '0')}:00",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = OceanTeal,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeSliderCompact(
    selectedDay: Int,
    selectedHour: Int,
    onDaySelected: (Int) -> Unit,
    onHourSelected: (Int) -> Unit
) {
    val dayLabels = (0..2).map { offset ->
        val date = java.time.LocalDate.now().plusDays(offset.toLong())
        when (offset) {
            0 -> "Today"
            1 -> "Tomorrow"
            else -> date.format(java.time.format.DateTimeFormatter.ofPattern("EEE d/M"))
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        // Day selector row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            dayLabels.forEachIndexed { index, label ->
                val isSelected = index == selectedDay
                Surface(
                    onClick = { onDaySelected(index) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) OceanBlue else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Hour slider (no hour grid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "00",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
            Slider(
                value = selectedHour.toFloat() / 23f,
                onValueChange = { onHourSelected((it * 23f).toInt().coerceIn(0, 23)) },
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = OceanBlue,
                    activeTrackColor = OceanBlue,
                    inactiveTrackColor = MaterialTheme.colorScheme.surface
                )
            )
            Text(
                text = "23",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
        }

        // Current hour label
        Text(
            text = "${selectedHour.toString().padStart(2, '0')}:00",
            style = MaterialTheme.typography.labelSmall,
            color = OceanTeal,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

private fun layerToggleIcon(layer: TileLayer) = when (layer) {
    TileLayer.TEMPERATURE -> Icons.Filled.Thermostat
    TileLayer.PRECIPITATION -> Icons.Filled.WaterDrop
    TileLayer.RAIN_CHANCE -> Icons.Filled.WaterDrop
    TileLayer.WIND -> Icons.Filled.Air
    TileLayer.PRESSURE -> Icons.Filled.Speed
    TileLayer.CLOUD -> Icons.Filled.Cloud
    TileLayer.UV -> Icons.Filled.Lightbulb
    TileLayer.SOLUNAR -> Icons.Filled.DarkMode
    TileLayer.RANKING -> Icons.Filled.EmojiEvents
}

@Composable
fun LocationSearchDialog(
    localLocations: List<ScotlandLocation>,
    searchResults: List<ScotlandLocation>,
    isSearching: Boolean,
    isRefreshing: Boolean,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onLocationSelected: (ScotlandLocation) -> Unit,
    onDismiss: () -> Unit
) {
    val filteredLocal = if (searchQuery.isBlank()) localLocations
        else localLocations.filter { it.name.contains(searchQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Search any location...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        text = {
            Column {
                if (isSearching || isRefreshing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (filteredLocal.isNotEmpty()) {
                    Text(
                        text = "Scotland Locations",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OceanTeal,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    filteredLocal.take(10).forEach { location ->
                        LocationItem(
                            location = location,
                            subtitle = "${"%.2f".format(location.lat)}, ${"%.2f".format(location.lon)}",
                            onClick = { onLocationSelected(location) }
                        )
                    }
                }

                if (searchResults.isNotEmpty()) {
                    if (filteredLocal.isNotEmpty()) Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OceanTeal,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    searchResults.take(8).forEach { location ->
                        LocationItem(
                            location = location,
                            subtitle = buildString {
                                if (location.region.isNotEmpty()) append(location.region)
                                if (location.country.isNotEmpty()) {
                                    if (isNotEmpty()) append(", ")
                                    append(location.country)
                                }
                            },
                            onClick = { onLocationSelected(location) }
                        )
                    }
                }

                if (filteredLocal.isEmpty() && searchResults.isEmpty() && !isSearching && searchQuery.length >= 3) {
                    Text(
                        text = "No results found. Try a different search term.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                if (searchQuery.length < 3 && searchQuery.isNotBlank()) {
                    Text(
                        text = "Type at least 3 characters to search",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun LocationItem(
    location: ScotlandLocation,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = OceanTeal,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
