package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(identifier) = LOWER(:identifier) LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET balanceUsd = balanceUsd + :amountUsd, balancePkr = balancePkr + :amountPkr WHERE id = :userId")
    suspend fun creditBalance(userId: Long, amountUsd: Double, amountPkr: Double)

    @Query("UPDATE users SET balanceUsd = balanceUsd - :amountUsd, balancePkr = balancePkr - :amountPkr WHERE id = :userId")
    suspend fun debitBalance(userId: Long, amountUsd: Double, amountPkr: Double)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
