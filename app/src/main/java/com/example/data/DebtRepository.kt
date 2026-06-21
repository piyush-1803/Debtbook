package com.example.data

import kotlinx.coroutines.flow.Flow

class DebtRepository(private val debtDao: DebtDao) {
    val allEntities: Flow<List<DbEntity>> = debtDao.getAllEntities()
    val allTransactions: Flow<List<DbTransaction>> = debtDao.getAllTransactions()

    suspend fun getEntityById(id: Long): DbEntity? = debtDao.getEntityById(id)

    suspend fun insertEntity(entity: DbEntity): Long = debtDao.insertEntity(entity)

    suspend fun deleteEntity(id: Long) {
        debtDao.deleteTransactionsByEntity(id)
        debtDao.deleteEntityById(id)
    }

    fun getTransactionsForEntity(entityId: Long): Flow<List<DbTransaction>> = 
        debtDao.getTransactionsForEntity(entityId)

    suspend fun insertTransaction(transaction: DbTransaction): Long = 
        debtDao.insertTransaction(transaction)

    suspend fun deleteTransaction(id: Long) = debtDao.deleteTransactionById(id)
}
