package com.danielsalas.danisalud.data

import kotlinx.coroutines.flow.Flow

class HealthRepository(private val healthDao: HealthDao) {
    val allBloodPressureRecords: Flow<List<BloodPressureRecord>> = healthDao.getAllBloodPressureRecords()
    val allWeightRecords: Flow<List<WeightRecord>> = healthDao.getAllWeightRecords()

    suspend fun insertBloodPressure(record: BloodPressureRecord) {
        healthDao.insertBloodPressure(record)
    }

    suspend fun deleteBloodPressure(record: BloodPressureRecord) {
        healthDao.deleteBloodPressure(record)
    }

    suspend fun insertWeight(record: WeightRecord) {
        healthDao.insertWeight(record)
    }

    suspend fun deleteWeight(record: WeightRecord) {
        healthDao.deleteWeight(record)
    }
}
