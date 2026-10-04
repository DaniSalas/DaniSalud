package com.danielsalas.danisalud.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOcrWeight: () -> Unit,
    viewModel: HealthViewModel,
    language: String
) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        Localization.get(language, "blood_pressure_tab"),
        Localization.get(language, "weight_tab")
    )

    var systolic by remember { mutableStateOf(viewModel.scannedSystolic) }
    var diastolic by remember { mutableStateOf(viewModel.scannedDiastolic) }
    var pulse by remember { mutableStateOf(viewModel.scannedPulse) }
    var weight by remember { mutableStateOf(viewModel.scannedWeight) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.scannedWeight) {
        if (viewModel.scannedWeight.isNotBlank()) {
            weight = viewModel.scannedWeight
            tabIndex = 1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.get(language, "add_measurement")) },
                navigationIcon = {
                    TextButton(onClick = {
                        viewModel.clearScannedData()
                        onNavigateBack()
                    }) {
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
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index; errorMessage = null },
                        text = { Text(title) }
                    )
                }
            }

            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (tabIndex == 0) {
                // Blood pressure: Fast, 100% reliable manual entry (as requested when OCR is not viable for 7-segment screens)
                OutlinedTextField(
                    value = systolic,
                    onValueChange = { systolic = it; errorMessage = null },
                    label = { Text(Localization.get(language, "systolic")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = diastolic,
                    onValueChange = { diastolic = it; errorMessage = null },
                    label = { Text(Localization.get(language, "diastolic")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pulse,
                    onValueChange = { pulse = it; errorMessage = null },
                    label = { Text(Localization.get(language, "pulse")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val sys = systolic.replace(",", ".").toFloatOrNull()
                        val dia = diastolic.replace(",", ".").toFloatOrNull()
                        val pul = pulse.toIntOrNull() ?: 0
                        if (sys != null && dia != null) {
                            viewModel.insertBloodPressure(sys, dia, pul)
                            viewModel.clearScannedData()
                            onNavigateBack()
                        } else {
                            errorMessage = "Si us plau, introdueix valors vàlids (amb punt o coma) per a la sistòlica i diastòlica."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(Localization.get(language, "save_blood_pressure"))
                }
            } else {
                // Weight: OCR works correctly here as confirmed by user
                Button(
                    onClick = onNavigateToOcrWeight,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(Localization.get(language, "scan_ocr_weight"))
                }

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it; errorMessage = null },
                    label = { Text(Localization.get(language, "weight_label")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val w = weight.replace(",", ".").toFloatOrNull()
                        if (w != null) {
                            viewModel.insertWeight(w)
                            viewModel.clearScannedData()
                            onNavigateBack()
                        } else {
                            errorMessage = "Si us plau, introdueix un valor de pes vàlid (amb punt o coma)."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(Localization.get(language, "save_weight"))
                }
            }
        }
    }
}
