package com.danielsalas.danisalud.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.danielsalas.danisalud.data.BloodPressureRecord
import com.danielsalas.danisalud.data.HealthRepository
import com.danielsalas.danisalud.data.WeightRecord
import kotlinx.coroutines.launch

class HealthViewModel(private val repository: HealthRepository) : ViewModel() {

    val allBloodPressureRecords: LiveData<List<BloodPressureRecord>> = repository.allBloodPressureRecords.asLiveData()
    val allWeightRecords: LiveData<List<WeightRecord>> = repository.allWeightRecords.asLiveData()

    // Temporary scanned OCR state
    var scannedSystolic by mutableStateOf("")
    var scannedDiastolic by mutableStateOf("")
    var scannedPulse by mutableStateOf("")
    var scannedWeight by mutableStateOf("")

    fun insertBloodPressure(systolic: Float, diastolic: Float, pulse: Int) {
        viewModelScope.launch {
            repository.insertBloodPressure(
                BloodPressureRecord(
                    systolic = systolic,
                    diastolic = diastolic,
                    pulse = pulse,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteBloodPressure(record: BloodPressureRecord) {
        viewModelScope.launch {
            repository.deleteBloodPressure(record)
        }
    }

    fun insertWeight(weight: Float) {
        viewModelScope.launch {
            repository.insertWeight(
                WeightRecord(
                    weight = weight,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteWeight(record: WeightRecord) {
        viewModelScope.launch {
            repository.deleteWeight(record)
        }
    }

    fun clearScannedData() {
        scannedSystolic = ""
        scannedDiastolic = ""
        scannedPulse = ""
        scannedWeight = ""
    }
}

class HealthViewModelFactory(private val repository: HealthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HealthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
