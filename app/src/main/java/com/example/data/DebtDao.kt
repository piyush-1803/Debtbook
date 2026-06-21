package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM entities ORDER BY name ASC")
    fun getAllEntities(): Flow<List<DbEntity>>

    @Query("SELECT * FROM entities WHERE id = :id LIMIT 1")
    suspend fun getEntityById(id: Long): DbEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntity(entity: DbEntity): Long

    @Query("DELETE FROM entities WHERE id = :id")
    suspend fun deleteEntityById(id: Long)

    @Query("SELECT * FROM transactions ORDER BY dateStamp DESC")
    fun getAllTransactions(): Flow<List<DbTransaction>>

    @Query("SELECT * FROM transactions WHERE entityId = :entityId ORDER BY dateStamp DESC")
    fun getTransactionsForEntity(entityId: Long): Flow<List<DbTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: DbTransaction): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions WHERE entityId = :entityId")
    suspend fun deleteTransactionsByEntity(entityId: Long)
}
