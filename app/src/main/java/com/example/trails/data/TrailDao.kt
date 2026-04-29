package com.example.trails.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrailDao {
    @Query("SELECT * FROM trails")
    fun getAllTrails(): Flow<List<Trail>>

    @Query("SELECT * FROM trails WHERE type = :type")
    fun getTrailsByType(type: String): Flow<List<Trail>>

    @Query("SELECT * FROM trails WHERE id = :id")
    fun getTrailById(id: Int): Flow<Trail?>

    @Query("SELECT COUNT(*) FROM trails")
    suspend fun getCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM trails WHERE isRunning = 1)")
    fun isAnyTrailActive(): Flow<Boolean>

    @Query("SELECT * FROM trails WHERE isRunning = 1 LIMIT 1")
    fun getActiveTrailFlow(): Flow<Trail?>

    @Query("SELECT * FROM trails WHERE isRunning = 1 LIMIT 1")
    suspend fun getActiveTrail(): Trail?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrails(trails: List<Trail>)

    @Update
    suspend fun updateTrail(trail: Trail)

    @Query("SELECT * FROM trails WHERE name LIKE '%' || :query || '%'")
    fun searchTrailsByName(query: String): Flow<List<Trail>>

    @Insert
    suspend fun insertRecord(record: TrailRecord)

    @Query("SELECT * FROM trail_records WHERE trailId = :trailId ORDER BY dateTimestamp DESC LIMIT 5")
    fun getLatestRecordsForTrail(trailId: Int): Flow<List<TrailRecord>>
    
    @Query("SELECT * FROM trail_records WHERE trailId = :trailId ORDER BY timeMillis ASC LIMIT 1")
    fun getBestRecordForTrail(trailId: Int): Flow<TrailRecord?>
}
