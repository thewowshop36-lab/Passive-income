package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DepositRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getDepositsForUser(userId: Long): Flow<List<DepositRecordEntity>>

    @Query("SELECT * FROM deposits ORDER BY createdAt DESC")
    fun getAllDeposits(): Flow<List<DepositRecordEntity>>

    @Query("SELECT * FROM deposits WHERE status = :status ORDER BY createdAt DESC")
    fun getDepositsByStatus(status: String): Flow<List<DepositRecordEntity>>

    @Query("SELECT * FROM deposits WHERE id = :id LIMIT 1")
    suspend fun getDepositById(id: Long): DepositRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositRecordEntity): Long

    @Update
    suspend fun updateDeposit(deposit: DepositRecordEntity)

    @Query("UPDATE deposits SET status = :status, adminNotes = :adminNotes, reviewedAt = :reviewedAt WHERE id = :depositId")
    suspend fun updateDepositStatus(depositId: Long, status: String, adminNotes: String?, reviewedAt: Long)
}
