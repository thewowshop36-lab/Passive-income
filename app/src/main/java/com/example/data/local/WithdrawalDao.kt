package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WithdrawalRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getWithdrawalsForUser(userId: Long): Flow<List<WithdrawalRecordEntity>>

    @Query("SELECT * FROM withdrawals ORDER BY createdAt DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalRecordEntity>>

    @Query("SELECT * FROM withdrawals WHERE status = :status ORDER BY createdAt DESC")
    fun getWithdrawalsByStatus(status: String): Flow<List<WithdrawalRecordEntity>>

    @Query("SELECT * FROM withdrawals WHERE id = :id LIMIT 1")
    suspend fun getWithdrawalById(id: Long): WithdrawalRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalRecordEntity): Long

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalRecordEntity)

    @Query("UPDATE withdrawals SET status = :status, adminNotes = :adminNotes, completedAt = :completedAt WHERE id = :withdrawalId")
    suspend fun updateWithdrawalStatus(withdrawalId: Long, status: String, adminNotes: String?, completedAt: Long)
}
