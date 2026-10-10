package com.danielsalas.danisalud.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_pressure_records")
data class BloodPressureRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val systolic: Float,
    val diastolic: Float,
    val pulse: Int,
    val timestamp: Long
)

@Entity(tableName = "weight_records")
data class WeightRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weight: Float,
    val timestamp: Long
)
