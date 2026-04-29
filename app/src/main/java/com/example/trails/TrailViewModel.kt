package com.example.trails

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trails.data.AppDatabase
import com.example.trails.data.Trail
import com.example.trails.data.TrailApi
import com.example.trails.data.TrailRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TrailViewModel(application: Application) : AndroidViewModel(application) {
    private val trailDao = AppDatabase.getDatabase(application, viewModelScope).trailDao()

    private val _selectedType = MutableStateFlow("all")
    val selectedType: StateFlow<String> = _selectedType

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress

    val trails: StateFlow<List<Trail>> = combine(
        trailDao.getAllTrails(),
        _selectedType,
        _searchQuery
    ) { allTrails, type, query ->
        allTrails.filter { trail ->
            val matchesType = when (type) {
                "all" -> true
                "favorite" -> trail.isFavorite
                else -> trail.type == type
            }
            val matchesQuery = query.isEmpty() || trail.name.contains(query, ignoreCase = true)
            matchesType && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isAnyTrailActive: StateFlow<Boolean> = trailDao.isAnyTrailActive()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val activeTrail: StateFlow<Trail?> = trailDao.getActiveTrailFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeElapsedTime: StateFlow<Long> = activeTrail
        .flatMapLatest { trail ->
            if (trail == null) flow { emit(0L) }
            else getElapsedTimeFlow(trail.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    init {
        refreshTrails()
    }

    private fun refreshTrails() {
        viewModelScope.launch {
            _loadingProgress.value = 0.1f
            insertDefaultTrails()
            _loadingProgress.value = 0.4f
            
            try {
                _loadingProgress.value = 0.5f
                val remoteTrails = TrailApi.retrofitService.getTrails()
                _loadingProgress.value = 0.8f
                if (remoteTrails.isNotEmpty()) {
                    trailDao.insertTrails(remoteTrails)
                }
            } catch (e: Exception) {
                Log.e("TrailViewModel", "Błąd podczas pobierania danych z API", e)
            } finally {
                _loadingProgress.value = 1.0f
            }
        }
    }

    private suspend fun insertDefaultTrails() {
        val existingTrails = trailDao.getAllTrails().first().associateBy { it.id }
        
        val defaultTrails = listOf(
            Trail(1, "Szlak Orlich Gniazd", "Piesza", "Niezapomniana trasa prowadząca przez jurajskie zamki.", "https://images.unsplash.com/photo-1524397057410-1e775ed476f3?auto=format&fit=crop&q=80&w=800"),
            Trail(2, "Velo Dunajec", "Rowerowa", "Jedna z najpiękniejszych tras rowerowych w Polsce.", "https://images.unsplash.com/photo-1562095241-8c6714fd4178?auto=format&fit=crop&q=80&w=800"),
            Trail(3, "Główny Szlak Beskidzki", "Piesza", "Najdłuższy szlak w polskich górach.", "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&q=80&w=800"),
            Trail(4, "Green Velo", "Rowerowa", "Wschodni Szlak Rowerowy przez unikalne regiony.", "https://images.unsplash.com/photo-1473341304170-971dccb5ac1e?auto=format&fit=crop&q=80&w=800"),
            Trail(5, "Dolina Chochołowska", "Piesza", "Urokliwa dolina, słynąca z wiosennych krokusów.", "https://images.unsplash.com/photo-1491555103944-7c647fd857e6?auto=format&fit=crop&q=80&w=800"),
            Trail(6, "Kaszubska Marszruta", "Rowerowa", "Sieć ścieżek rowerowych w Borach Tucholskich.", "https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&q=80&w=800"),
            Trail(7, "Morskie Oko", "Piesza", "Najpopularniejszy szlak w Tatrach do jeziora polodowcowego.", "https://images.unsplash.com/photo-1589182373726-e4f658ab50f0?auto=format&fit=crop&q=80&w=800"),
            Trail(8, "Szlak wokół Jeziora Czorsztyńskiego", "Rowerowa", "Widokowa trasa z zamkami w tle.", "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&q=80&w=800"),
            Trail(9, "Połonina Wetlińska", "Piesza", "Klasyk bieszczadzki z niesamowitymi panoramami.", "https://images.unsplash.com/photo-1454496522488-7a8e488e8606?auto=format&fit=crop&q=80&w=800"),
            Trail(10, "EuroVelo 10", "Rowerowa", "Nadmorska trasa wzdłuż polskiego wybrzeża Bałtyku.", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&q=80&w=800"),
            Trail(11, "Szlak na Tarnicę", "Piesza", "Zdobycie najwyższego szczytu polskich Bieszczadów.", "https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&q=80&w=800"),
            Trail(12, "Wiślana Trasa Rowerowa", "Rowerowa", "Trasa biegnąca wzdłuż największej polskiej rzeki.", "https://images.unsplash.com/photo-1518173946687-a4c8892bbd9f?auto=format&fit=crop&q=80&w=800"),
            Trail(13, "Ścieżka nad Reglami", "Piesza", "Piękna trasa reglowa w Tatrach Zachodnich.", "https://images.unsplash.com/photo-1533512930330-4ac257c86793?auto=format&fit=crop&q=80&w=800"),
            Trail(14, "Szlak wokół Tatr", "Rowerowa", "Transgraniczna pętla rowerowa wokół najwyższych gór.", "https://images.unsplash.com/photo-1519904981063-b0cf448d479e?auto=format&fit=crop&q=80&w=800"),
            Trail(15, "Szlak na Giewont", "Piesza", "Kultowa trasa na śpiącego rycerza z krzyżem.", "https://images.unsplash.com/photo-1483728642387-6c3bdd6c93e5?auto=format&fit=crop&q=80&w=800"),
            Trail(16, "Velo Metropolis", "Rowerowa", "Małopolska część trasy EuroVelo 4.", "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&q=80&w=800"),
            Trail(17, "Sokola Perć", "Piesza", "Ekscytujący szlak graniowy w Pieninach Środkowych.", "https://images.unsplash.com/photo-1589182373726-e4f658ab50f0?auto=format&fit=crop&q=80&w=800"),
            Trail(18, "Pętla Żuławska", "Rowerowa", "Trasa przez depresyjne tereny i unikalne zabytki.", "https://images.unsplash.com/photo-1449034446853-66c86144b0ad?auto=format&fit=crop&q=80&w=800"),
            Trail(19, "Szlak Latarni Morskich", "Piesza", "Wędrówka brzegiem morza łącząca zabytkowe obiekty.", "https://images.unsplash.com/photo-1505852679233-d9fd70aff56d?auto=format&fit=crop&q=80&w=800"),
            Trail(20, "Trasa rowerowa po Puszczy Białowieskiej", "Rowerowa", "Kontakt z dziką naturą w sercu puszczy.", "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&q=80&w=800"),
            Trail(21, "Babia Góra - Perć Akademików", "Piesza", "Najtrudniejszy szlak w Beskidach z elementami wspinaczki.", "https://images.unsplash.com/photo-1551632811-561732d1e306?auto=format&fit=crop&q=80&w=800"),
            Trail(22, "Velo Natura", "Rowerowa", "Trasa biegnąca przez malownicze tereny doliny Popradu.", "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?auto=format&fit=crop&q=80&w=800"),
            Trail(23, "Szlak architektury drewnianej", "Piesza", "Odkrywanie zabytkowych cerkwi i kościołów.", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&q=80&w=800"),
            Trail(24, "Szlaki Gór Stołowych", "Piesza", "Wędrówka wśród fantastycznych form skalnych.", "https://images.unsplash.com/photo-1500622944204-b135684e99fd?auto=format&fit=crop&q=80&w=800")
        ).map { defaultTrail ->
            existingTrails[defaultTrail.id]?.let { existing ->
                defaultTrail.copy(
                    startTime = existing.startTime,
                    isRunning = existing.isRunning,
                    totalTime = existing.totalTime,
                    isFavorite = existing.isFavorite
                )
            } ?: defaultTrail
        }

        trailDao.insertTrails(defaultTrails)
    }

    fun updateTrailType(type: String) {
        _selectedType.value = type
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(trail: Trail) {
        viewModelScope.launch {
            trailDao.updateTrail(trail.copy(isFavorite = !trail.isFavorite))
        }
    }

    fun getTrailById(id: Int): Flow<Trail?> {
        return trailDao.getTrailById(id)
    }

    fun startTimer(trail: Trail) {
        viewModelScope.launch {
            val activeTrail = trailDao.getActiveTrail()
            if (activeTrail == null) {
                val updatedTrail = trail.copy(
                    startTime = System.currentTimeMillis(),
                    isRunning = true
                )
                trailDao.updateTrail(updatedTrail)
            }
        }
    }

    fun stopTimer(trail: Trail) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val sessionTime = if (trail.isRunning && trail.startTime != null) {
                currentTime - trail.startTime
            } else {
                0L
            }
            
            val totalSessionTime = trail.totalTime + sessionTime

            if (totalSessionTime > 0) {
                trailDao.insertRecord(
                    TrailRecord(
                        trailId = trail.id,
                        timeMillis = totalSessionTime
                    )
                )
            }

            val updatedTrail = trail.copy(
                startTime = null,
                isRunning = false,
                totalTime = 0L
            )
            trailDao.updateTrail(updatedTrail)
        }
    }

    fun resetTimer(trail: Trail) {
        viewModelScope.launch {
            val updatedTrail = trail.copy(
                startTime = null,
                isRunning = false,
                totalTime = 0L
            )
            trailDao.updateTrail(updatedTrail)
        }
    }
    
    fun getElapsedTimeFlow(trailId: Int): Flow<Long> = flow {
        while (true) {
            val trail = trailDao.getTrailById(trailId).first()
            if (trail != null) {
                val elapsed = if (trail.isRunning && trail.startTime != null) {
                    trail.totalTime + (System.currentTimeMillis() - trail.startTime)
                } else {
                    trail.totalTime
                }
                emit(elapsed)
            }
            delay(1000)
        }
    }

    fun getLatestRecords(trailId: Int): Flow<List<TrailRecord>> {
        return trailDao.getLatestRecordsForTrail(trailId)
    }
}
