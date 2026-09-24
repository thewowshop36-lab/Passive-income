package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AdWatchLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdWatchDao {
    @Query("SELECT * FROM ad_watch_logs WHERE userId = :userId ORDER BY watchedAt DESC LIMIT 50")
    fun getLogsForUser(userId: Long): Flow<List<AdWatchLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AdWatchLogEntity): Long

    @Query("SELECT COUNT(*) FROM ad_watch_logs WHERE userId = :userId")
    suspend fun getTotalAdsWatchedByUser(userId: Long): Int
}
