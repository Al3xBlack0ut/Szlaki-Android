package com.example.trails.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "trails")
data class Trail(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String, // e.g., "Piesza", "Rowerowa"
    val description: String,
    val imageUrl: String,
    val startTime: Long? = null,
    val isRunning: Boolean = false,
    val totalTime: Long = 0, // accumulated time in milliseconds
    val isFavorite: Boolean = false
)
