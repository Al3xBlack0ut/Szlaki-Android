package com.example.trails

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.trails.data.Trail
import com.example.trails.ui.theme.TrailsTheme
import com.example.trails.ui.theme.WalkBadgeLight

class DetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val trailId = intent.getIntExtra("TRAIL_ID", -1)

        setContent {
            TrailsTheme {
                val trailViewModel: TrailViewModel = viewModel()
                val trails by trailViewModel.trails.collectAsStateWithLifecycle()
                
                if (trails.isNotEmpty()) {
                    val initialPage = trails.indexOfFirst { it.id == trailId }.coerceAtLeast(0)
                    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { trails.size })
                    
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        DetailsScreen(
                            trail = trails[page],
                            onBack = { finish() },
                            viewModel = trailViewModel
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("LocalContextResourcesRead", "DiscouragedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    trail: Trail,
    onBack: () -> Unit,
    viewModel: TrailViewModel
) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val elapsedTime by viewModel.getElapsedTimeFlow(trail.id).collectAsState(initial = 0L)
    val isAnyActive by viewModel.isAnyTrailActive.collectAsStateWithLifecycle()
    val activeTrail by viewModel.activeTrail.collectAsStateWithLifecycle()
    val records by viewModel.getLatestRecords(trail.id).collectAsState(initial = emptyList())
    val configuration = LocalConfiguration.current
    
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.smallestScreenWidthDp >= 600
    val isPhoneLandscape = isLandscape && !isTablet
    
    // Kolor adaptacyjny: biały w Dark Mode, granatowy w Light Mode
    val adaptiveColor = if (darkTheme) Color.White else WalkBadgeLight
    
    val imageModel = remember(trail.imageUrl) {
        if (trail.imageUrl.startsWith("http")) {
            trail.imageUrl
        } else {
            val resName = trail.imageUrl.substringBeforeLast(".")
            val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (id != 0) id else trail.imageUrl
        }
    }

    val colorFilter = if (darkTheme) {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToScale(0.7f, 0.7f, 0.7f, 1f) })
    } else null
    
    val subduedWhite = if (darkTheme) Color(0xFFE6E1E5) else Color.White

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
                            .height(40.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Wstecz",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Szczegóły", 
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.toggleFavorite(trail) }) {
                            Icon(
                                imageVector = if (trail.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Ulubione",
                                tint = if (trail.isFavorite) (if (darkTheme) Color.White else MaterialTheme.colorScheme.onPrimaryContainer) else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else {
                TopAppBar(
                    title = { 
                        Text(
                            "Szczegóły", 
                            fontWeight = FontWeight.Bold,
                            style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleFavorite(trail) }) {
                            Icon(
                                imageVector = if (trail.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Ulubione",
                                tint = if (trail.isFavorite) (if (darkTheme || !isLandscape) Color.White else adaptiveColor) else (if (isLandscape) MaterialTheme.colorScheme.onPrimaryContainer else subduedWhite)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isLandscape) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        navigationIconContentColor = if (isLandscape) MaterialTheme.colorScheme.onPrimaryContainer else subduedWhite,
                        titleContentColor = if (isLandscape) MaterialTheme.colorScheme.onPrimaryContainer else subduedWhite,
                        actionIconContentColor = if (isLandscape) MaterialTheme.colorScheme.onPrimaryContainer else subduedWhite
                    )
                )
            }
        },
        floatingActionButton = {
            if (records.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { 
                        val lastTime = formatTime(records.first().timeMillis)
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Mój czas na trasie ${trail.name} to $lastTime!")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Udostępnij")
                }
            }
        }
    ) { innerPadding ->
        if (isLandscape) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = trail.name,
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .fillMaxHeight()
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop,
                    colorFilter = colorFilter,
                    error = painterResource(id = android.R.drawable.ic_menu_report_image)
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(if (isPhoneLandscape) 0.65f else 0.6f)
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(if (isPhoneLandscape) 16.dp else 24.dp)
                    ) {
                        TrailInfoContent(
                            trail = trail,
                            elapsedTime = elapsedTime,
                            isAnyActive = isAnyActive,
                            activeTrail = activeTrail,
                            records = records,
                            viewModel = viewModel,
                            isPhoneLandscape = isPhoneLandscape
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Box(modifier = Modifier.height(300.dp)) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = trail.name,
                        modifier = Modifier.fillMaxSize().background(Color.Gray),
                        contentScale = ContentScale.Crop,
                        colorFilter = colorFilter,
                        error = painterResource(id = android.R.drawable.ic_menu_report_image)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(24.dp)
                ) {
                    TrailInfoContent(
                        trail = trail,
                        elapsedTime = elapsedTime,
                        isAnyActive = isAnyActive,
                        activeTrail = activeTrail,
                        records = records,
                        viewModel = viewModel,
                        isPhoneLandscape = false
                    )
                    
                    Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 80.dp))
                }
            }
        }
    }
}

@Composable
fun TrailInfoContent(
    trail: Trail,
    elapsedTime: Long,
    isAnyActive: Boolean,
    activeTrail: Trail?,
    records: List<com.example.trails.data.TrailRecord>,
    viewModel: TrailViewModel,
    isPhoneLandscape: Boolean
) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    
    // Spójna paleta kolorów: Biały w Dark Mode, Granatowy w Light Mode
    val adaptiveColor = if (darkTheme) Color.White else WalkBadgeLight
    val primaryHeaderColor = if (darkTheme) Color.White else WalkBadgeLight
    val disabledContentColor = if (darkTheme) Color(0xFF49454F) else Color.Gray

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (trail.type == "Piesza") Icons.AutoMirrored.Filled.DirectionsWalk else Icons.AutoMirrored.Filled.DirectionsBike,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(if (isPhoneLandscape) 18.dp else 22.dp)
                )
                Text(
                    text = trail.type,
                    style = if (isPhoneLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = formatTime(elapsedTime),
                style = if (isPhoneLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(if (isPhoneLandscape) 12.dp else 24.dp))

    Text(
        text = trail.name,
        style = if (isPhoneLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = adaptiveColor
    )

    Spacer(modifier = Modifier.height(if (isPhoneLandscape) 16.dp else 32.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!trail.isRunning) {
            Button(
                onClick = { 
                    if (!isAnyActive) {
                        viewModel.startTimer(trail)
                    } else if (activeTrail != null) {
                        val intent = Intent(context, DetailsActivity::class.java).apply {
                            putExtra("TRAIL_ID", activeTrail.id)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier.weight(1f).height(if (isPhoneLandscape) 40.dp else 56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAnyActive) Color.LightGray else Color(0xFF4CAF50),
                    contentColor = if (isAnyActive) disabledContentColor else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = if (isAnyActive) Icons.Default.Lock else Icons.Default.PlayArrow, 
                    contentDescription = null, 
                    modifier = Modifier.size(if (isPhoneLandscape) 20.dp else 24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Rozpocznij", style = if (isPhoneLandscape) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium)
            }
        } else {
            Button(
                onClick = { viewModel.stopTimer(trail) },
                modifier = Modifier.weight(1f).height(if (isPhoneLandscape) 40.dp else 56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(if (isPhoneLandscape) 20.dp else 24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Zatrzymaj", style = if (isPhoneLandscape) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium)
            }
        }
        
        OutlinedButton(
            onClick = { viewModel.resetTimer(trail) },
            modifier = Modifier.weight(1f).height(if (isPhoneLandscape) 40.dp else 56.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = adaptiveColor)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(if (isPhoneLandscape) 20.dp else 24.dp))
            Spacer(Modifier.width(8.dp))
            Text("Resetuj", style = if (isPhoneLandscape) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium)
        }
    }

    Spacer(modifier = Modifier.height(if (isPhoneLandscape) 24.dp else 40.dp))

    Text(
        text = "O trasie",
        style = if (isPhoneLandscape) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = primaryHeaderColor
    )

    Spacer(modifier = Modifier.height(if (isPhoneLandscape) 8.dp else 12.dp))

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = trail.description,
            style = if (isPhoneLandscape) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            lineHeight = if (isPhoneLandscape) 20.sp else 28.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }

    if (records.isNotEmpty()) {
        Spacer(modifier = Modifier.height(if (isPhoneLandscape) 24.dp else 40.dp))
        Text(
            text = "Ostatnie czasy",
            style = if (isPhoneLandscape) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = primaryHeaderColor
        )
        Spacer(modifier = Modifier.height(12.dp))
        records.forEach { record ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(record.dateTimestamp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatTime(record.timeMillis),
                        fontWeight = FontWeight.Bold,
                        color = adaptiveColor
                    )
                }
            }
        }
    }
}
