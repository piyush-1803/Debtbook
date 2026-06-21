package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entities")
data class DbEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val relationship: String = "Friend", // "Friend", "Client", "Supplier", "Family"
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class DbTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val description: String,
    val type: String, // "PAYABLE" (I owe them) or "RECEIVABLE" (They owe me)
    val category: String, // "Groceries", "Dining", "Travel", "Rent", "Fun", "Health", "Bills", "Other"
    val dateStamp: Long = System.currentTimeMillis(),
    val entityId: Long // Foreign key to DbEntity
)
