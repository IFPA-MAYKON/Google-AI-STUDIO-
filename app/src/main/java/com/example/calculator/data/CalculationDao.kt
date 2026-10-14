package com.example.calculator.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calc: CalculationEntity): Long

    @Delete
    suspend fun deleteCalculation(calc: CalculationEntity)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAllHistory()

    @Query("SELECT COUNT(*) FROM calculation_history")
    fun getHistoryCount(): Flow<Int>
}
