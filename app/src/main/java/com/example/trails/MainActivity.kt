package com.example.trails

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.trails.data.Trail
import com.example.trails.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            TrailsTheme {
                TrailScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailScreen(viewModel: TrailViewModel = viewModel()) {
    val trails by viewModel.trails.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeTrail by viewModel.activeTrail.collectAsStateWithLifecycle()
    val activeTime by viewModel.activeElapsedTime.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    val screenWidth = configuration.screenWidthDp.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = screenWidth >= 600.dp
    val isPhoneLandscape = isLandscape && !isTablet

    var isSearchActive by remember { mutableStateOf(false) }

    val columns = when {
        configuration.screenWidthDp > 900 -> 3
        configuration.screenWidthDp > 600 -> 2
        else -> 1
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    "TRASY",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Wszystkie") },
                    selected = selectedType == "all",
                    onClick = { 
                        viewModel.updateTrailType("all")
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Ulubione") },
                    selected = selectedType == "favorite",
                    onClick = { 
                        viewModel.updateTrailType("favorite")
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Favorite, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Piesze") },
                    selected = selectedType == "Piesza",
                    onClick = { 
                        viewModel.updateTrailType("Piesza")
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Rowerowe") },
                    selected = selectedType == "Rowerowa",
                    onClick = { 
                        viewModel.updateTrailType("Rowerowa")
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.DirectionsBike, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "Wersja 1.0",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (isPhoneLandscape) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            if (!isSearchActive) {
                                Text(
                                    text = "Odkryj Trasy",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            if (isSearchActive) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    placeholder = { Text("Szukaj...", style = MaterialTheme.typography.bodySmall) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    trailingIcon = {
                                        IconButton(onClick = { 
                                            isSearchActive = false
                                            viewModel.updateSearchQuery("")
                                        }) {
                                            Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(24.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "Szukaj", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }
                } else {
                    TopAppBar(
                        title = { 
                            if (!isSearchActive) {
                                Text(
                                    text = "Odkryj Trasy", 
                                    fontWeight = FontWeight.Black,
                                    style = if (isLandscape && !isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall
                                )
                            } else {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 8.dp)
                                        .height(52.dp),
                                    placeholder = { Text("Szukaj trasy...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon = {
                                        IconButton(onClick = { 
                                            isSearchActive = false
                                            viewModel.updateSearchQuery("")
                                        }) {
                                            Icon(Icons.Default.Close, null)
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(26.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            if (!isSearchActive) {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "Szukaj")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isLandscape) {
                    val elementHeight = if (isPhoneLandscape) 40.dp else 56.dp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            FilterChips(
                                selectedType = selectedType,
                                onTypeSelected = { viewModel.updateTrailType(it) },
                                fullWidth = true,
                                height = elementHeight,
                                isLandscape = true,
                                isTablet = isTablet,
                                isPhoneLandscape = isPhoneLandscape
                            )
                        }
                        
                        if (activeTrail != null) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                ActiveTimerBox(
                                    activeTrail = activeTrail,
                                    activeTime = activeTime,
                                    height = elementHeight,
                                    isTablet = isTablet,
                                    isPhoneLandscape = isPhoneLandscape,
                                    onClick = { trail ->
                                        val intent = Intent(context, DetailsActivity::class.java).apply {
                                            putExtra("TRAIL_ID", trail.id)
                                        }
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    val verticalTimerHeight = if (isTablet) 64.dp else 56.dp
                    ActiveTimerBox(
                        activeTrail = activeTrail,
                        activeTime = activeTime,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        height = verticalTimerHeight,
                        isTablet = isTablet,
                        isPhoneLandscape = false,
                        onClick = { trail ->
                            val intent = Intent(context, DetailsActivity::class.java).apply {
                                putExtra("TRAIL_ID", trail.id)
                            }
                            context.startActivity(intent)
                        }
                    )
                    
                    FilterChips(
                        selectedType = selectedType,
                        onTypeSelected = { viewModel.updateTrailType(it) },
                        isLandscape = false,
                        isTablet = isTablet,
                        isPhoneLandscape = false
                    )
                }

                if (columns > 1) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(trails, key = { it.id }) { trail ->
                            TrailCard(
                                trail = trail,
                                onTypeClick = { type -> viewModel.updateTrailType(type) },
                                onToggleFavorite = { viewModel.toggleFavorite(trail) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(trails, key = { it.id }) { trail ->
                            TrailCard(
                                trail = trail,
                                onTypeClick = { type -> viewModel.updateTrailType(type) },
                                onToggleFavorite = { viewModel.toggleFavorite(trail) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveTimerBox(
    activeTrail: Trail?,
    activeTime: Long,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 48.dp,
    isTablet: Boolean = false,
    isPhoneLandscape: Boolean = false,
    onClick: (Trail) -> Unit
) {
    AnimatedVisibility(
        visible = activeTrail != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        activeTrail?.let { trail ->
            val navyColor = MaterialTheme.colorScheme.onPrimaryContainer
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClick(trail) },
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Timer, 
                        contentDescription = null,
                        tint = navyColor,
                        modifier = Modifier.size(if (isPhoneLandscape) 14.dp else if (isTablet) 24.dp else 18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${trail.name}: ${formatTime(activeTime)}",
                        style = when {
                            isPhoneLandscape -> MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                            isTablet -> MaterialTheme.typography.titleMedium
                            else -> MaterialTheme.typography.labelMedium
                        },
                        fontWeight = FontWeight.Bold,
                        color = navyColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    fullWidth: Boolean = false,
    height: androidx.compose.ui.unit.Dp = 48.dp,
    isLandscape: Boolean = false,
    isTablet: Boolean = false,
    isPhoneLandscape: Boolean = false
) {
    val navyColor = MaterialTheme.colorScheme.onPrimaryContainer
    val chipColors = FilterChipDefaults.filterChipColors(
        labelColor = navyColor,
        selectedLabelColor = navyColor,
        iconColor = navyColor,
        selectedLeadingIconColor = navyColor
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (fullWidth) 0.dp else 16.dp, vertical = if (fullWidth) 0.dp else 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val chipModifier = Modifier.weight(1f).height(height)
        val iconSize = if (isPhoneLandscape) 18.dp else if (isTablet) 28.dp else 24.dp
        
        FilterChip(
            selected = selectedType == "all",
            onClick = { onTypeSelected("all") },
            label = { 
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Wszystkie", modifier = Modifier.size(iconSize)) 
                }
            },
            modifier = chipModifier,
            shape = RoundedCornerShape(12.dp),
            colors = chipColors
        )
        FilterChip(
            selected = selectedType == "favorite",
            onClick = { onTypeSelected("favorite") },
            label = { 
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Favorite, contentDescription = "Ulubione", modifier = Modifier.size(iconSize))
                }
            },
            modifier = chipModifier,
            shape = RoundedCornerShape(12.dp),
            colors = chipColors
        )
        FilterChip(
            selected = selectedType == "Piesza",
            onClick = { onTypeSelected("Piesza") },
            label = { 
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = "Piesze", modifier = Modifier.size(iconSize))
                }
            },
            modifier = chipModifier,
            shape = RoundedCornerShape(12.dp),
            colors = chipColors
        )
        FilterChip(
            selected = selectedType == "Rowerowa",
            onClick = { onTypeSelected("Rowerowa") },
            label = { 
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = "Rowerowe", modifier = Modifier.size(iconSize))
                }
            },
            modifier = chipModifier,
            shape = RoundedCornerShape(12.dp),
            colors = chipColors
        )
    }
}

@Composable
fun TrailCard(
    trail: Trail,
    onTypeClick: (String) -> Unit,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val density = LocalDensity.current
    
    val imageModel = remember(trail.imageUrl) {
        if (trail.imageUrl.startsWith("http")) {
            trail.imageUrl
        } else {
            val name = trail.imageUrl.substringBeforeLast(".")
            val id = context.resources.getIdentifier(name, "drawable", context.packageName)
            if (id != 0) id else trail.imageUrl
        }
    }

    val colorFilter = if (darkTheme) {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToScale(0.7f, 0.7f, 0.7f, 1f) })
    } else null
    
    val typography = MaterialTheme.typography
    val adaptiveColor = if (darkTheme) Color.White else WalkBadgeLight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp) 
            .clickable {
                val intent = Intent(context, DetailsActivity::class.java).apply {
                    putExtra("TRAIL_ID", trail.id)
                }
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = imageModel,
                    contentDescription = trail.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop,
                    colorFilter = colorFilter,
                    error = painterResource(id = android.R.drawable.ic_menu_report_image)
                )
                
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (trail.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Ulubione",
                        tint = if (trail.isFavorite) adaptiveColor else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp).fillMaxHeight()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trail.name,
                        style = typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveColor,
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    val iconSize = remember(density) { 
                        with(density) { (typography.titleLarge.fontSize.toDp() * 1.3f) }
                    }
                    
                    IconButton(
                        onClick = { onTypeClick(trail.type) },
                        modifier = Modifier.size(iconSize + 8.dp)
                    ) {
                        Icon(
                            imageVector = if (trail.type == "Piesza") Icons.AutoMirrored.Filled.DirectionsWalk else Icons.AutoMirrored.Filled.DirectionsBike,
                            contentDescription = "Filtruj: ${trail.type}",
                            tint = if (trail.type == "Piesza") {
                                if (darkTheme) WalkBadgeDark else WalkBadgeLight
                            } else {
                                if (darkTheme) BikeBadgeDark else BikeBadgeLight
                            },
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = trail.description,
                    style = typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
