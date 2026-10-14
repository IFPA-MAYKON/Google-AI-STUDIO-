package com.example.calculator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expression: String,
    val result: String,
    val numericResult: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String = "PADRÃO" // "PADRÃO" or "CIENTÍFICA"
)
