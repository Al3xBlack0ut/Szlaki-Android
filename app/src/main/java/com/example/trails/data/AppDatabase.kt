package com.example.trails.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Trail::class, TrailRecord::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trailDao(): TrailDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trails_database"
                )
                    .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            populate()
        }

        private fun populate() {
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val trailDao = database.trailDao()
                    val trails = listOf(
                        Trail(
                            id = 10,
                            name = "Tatrzańska Grań",
                            type = "Piesza",
                            description = "Wymagająca trasa dla doświadczonych turystów z pięknymi widokami na Tatry Wysokie.",
                            imageUrl = "https://images.unsplash.com/photo-1589182373726-e4f658ab50f0?q=80&w=1000&auto=format&fit=crop"
                        ),
                        Trail(
                            id = 11,
                            name = "Szlak wokół Tatr",
                            type = "Rowerowa",
                            description = "Pętla rowerowa łącząca polskie i słowackie Podtatrze.",
                            imageUrl = "https://images.unsplash.com/photo-1541625602330-2277a4c4b08d?q=80&w=1000&auto=format&fit=crop"
                        )
                    )
                    trailDao.insertTrails(trails)
                }
            }
        }
    }
}
