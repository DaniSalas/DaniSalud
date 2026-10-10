package com.danielsalas.danisalud.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddEntry: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HealthViewModel,
    userName: String,
    language: String
) {
    val bpRecords by viewModel.allBloodPressureRecords.observeAsState(emptyList())
    val weightRecords by viewModel.allWeightRecords.observeAsState(emptyList())

    val latestBp = bpRecords.firstOrNull()
    val latestWeight = weightRecords.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userName) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = Localization.get(language, "settings"))
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = Localization.get(language, "last_blood_pressure"), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (latestBp != null) {
                        val dateStr = DateFormat.format("dd/MM/yyyy HH:mm", Date(latestBp.timestamp)).toString()
                        val pulseLabel = Localization.get(language, "pulse_short")
                        Text(
                            text = "${latestBp.systolic} / ${latestBp.diastolic} mmHg ($pulseLabel: ${latestBp.pulse})",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(text = dateStr, style = MaterialTheme.typography.labelSmall)
                    } else {
                        Text(text = Localization.get(language, "no_blood_pressure"), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = Localization.get(language, "last_weight"), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (latestWeight != null) {
                        val dateStr = DateFormat.format("dd/MM/yyyy HH:mm", Date(latestWeight.timestamp)).toString()
                        Text(
                            text = "${latestWeight.weight} kg",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(text = dateStr, style = MaterialTheme.typography.labelSmall)
                    } else {
                        Text(text = Localization.get(language, "no_weight"), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = onNavigateToAddEntry, modifier = Modifier.fillMaxWidth()) {
                Text(Localization.get(language, "add_measurement"))
            }
            OutlinedButton(onClick = onNavigateToHistory, modifier = Modifier.fillMaxWidth()) {
                Text(Localization.get(language, "view_history"))
            }
        }
    }
}
