package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deposits")
data class DepositRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val userIdentifier: String,
    val method: String, // "USDT (TRC20)", "JazzCash", "EasyPaisa"
    val amount: Double,
    val currency: String, // "USDT", "PKR"
    val creditedUsd: Double,
    val creditedPkr: Double,
    val transactionId: String,
    val senderDetails: String,
    val screenshotUri: String? = null,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val adminNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null
)
