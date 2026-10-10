package com.danielsalas.danisalud.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danielsalas.danisalud.data.BloodPressureRecord
import com.danielsalas.danisalud.data.WeightRecord
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HealthViewModel,
    language: String
) {
    val bpRecords by viewModel.allBloodPressureRecords.observeAsState(emptyList())
    val weightRecords by viewModel.allWeightRecords.observeAsState(emptyList())

    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        Localization.get(language, "blood_pressure_tab"),
        Localization.get(language, "weight_tab")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.get(language, "medical_history")) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(Localization.get(language, "back"))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = tabIndex == index, onClick = { tabIndex = index }, text = { Text(title) })
                }
            }

            if (tabIndex == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bpRecords, key = { it.id }) { record ->
                        BloodPressureItem(record = record, onDelete = { viewModel.deleteBloodPressure(record) }, language = language)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(weightRecords, key = { it.id }) { record ->
                        WeightItem(record = record, onDelete = { viewModel.deleteWeight(record) }, language = language)
                    }
                }
            }
        }
    }
}

@Composable
fun BloodPressureItem(record: BloodPressureRecord, onDelete: () -> Unit, language: String) {
    val dateString = DateFormat.format("dd/MM/yyyy HH:mm", Date(record.timestamp)).toString()
    val pulseLabel = Localization.get(language, "pulse_short")
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "${record.systolic} / ${record.diastolic} mmHg", style = MaterialTheme.typography.titleLarge)
                Text(text = "$pulseLabel: ${record.pulse} bpm", style = MaterialTheme.typography.bodyMedium)
                Text(text = dateString, style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = onDelete) {
                Text(Localization.get(language, "delete"), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun WeightItem(record: WeightRecord, onDelete: () -> Unit, language: String) {
    val dateString = DateFormat.format("dd/MM/yyyy HH:mm", Date(record.timestamp)).toString()
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "${record.weight} kg", style = MaterialTheme.typography.titleLarge)
                Text(text = dateString, style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = onDelete) {
                Text(Localization.get(language, "delete"), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
