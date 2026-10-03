package com.danielsalas.danisalud.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM blood_pressure_records ORDER BY timestamp DESC")
    fun getAllBloodPressureRecords(): Flow<List<BloodPressureRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodPressure(record: BloodPressureRecord)

    @Delete
    suspend fun deleteBloodPressure(record: BloodPressureRecord)

    @Query("SELECT * FROM weight_records ORDER BY timestamp DESC")
    fun getAllWeightRecords(): Flow<List<WeightRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(record: WeightRecord)

    @Delete
    suspend fun deleteWeight(record: WeightRecord)
}
