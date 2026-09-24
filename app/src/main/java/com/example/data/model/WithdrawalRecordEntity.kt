package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "withdrawals")
data class WithdrawalRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val userIdentifier: String,
    val method: String, // "USDT TRC20", "JazzCash", "EasyPaisa"
    val amount: Double,
    val currency: String, // "USD", "PKR"
    val accountTitle: String,
    val accountIdentifier: String, // Wallet address or phone/account number
    val status: String = "PENDING", // "PENDING", "COMPLETED", "REJECTED"
    val adminNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
