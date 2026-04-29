package com.example.trails.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "trail_records",
    foreignKeys = [
        ForeignKey(
            entity = Trail::class,
            parentColumns = ["id"],
            childColumns = ["trailId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TrailRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val trailId: Int,
    val timeMillis: Long,
    val dateTimestamp: Long = System.currentTimeMillis()
)
