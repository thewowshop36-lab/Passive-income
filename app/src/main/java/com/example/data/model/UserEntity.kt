package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val identifier: String, // Email or Phone number
    val fullName: String,
    val passwordHash: String,
    val balanceUsd: Double = 0.0,
    val balancePkr: Double = 0.0,
    val activePlanId: Int? = null,
    val activePlanName: String? = null,
    val planDailyAdLimit: Int = 0,
    val planPrice: Double = 0.0,
    val planActivatedAt: Long? = null,
    val adsWatchedToday: Int = 0,
    val lastAdWatchDate: String = "", // e.g. "2026-09-24"
    val lastAdWatchTimestamp: Long = 0L,
    val totalEarnedUsd: Double = 0.0,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
