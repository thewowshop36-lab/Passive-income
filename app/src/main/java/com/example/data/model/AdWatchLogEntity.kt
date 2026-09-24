package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ad_watch_logs")
data class AdWatchLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val adTitle: String,
    val rewardUsd: Double = 0.05,
    val rewardPkr: Double = 0.05 * 280.0,
    val watchedAt: Long = System.currentTimeMillis()
)
