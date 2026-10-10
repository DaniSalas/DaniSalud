package com.danielsalas.danisalud.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danielsalas.danisalud.data.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    preferencesManager: PreferencesManager,
    onSettingsChanged: () -> Unit
) {
    var userName by remember { mutableStateOf(preferencesManager.userName) }
    var selectedLang by remember { mutableStateOf(preferencesManager.language) }
    var selectedTheme by remember { mutableStateOf(preferencesManager.darkMode) }

    val languages = listOf(
        "ca" to "Català",
        "eu" to "Euskera",
        "gl" to "Gallego",
        "an" to "Andaluz",
        "en" to "English",
        "fr" to "Français",
        "de" to "Deutsch",
        "zh" to "中文",
        "ko" to "한국어",
        "es-la" to "Español Latino"
    )

    val themes = listOf(
        "system" to Localization.get(selectedLang, "theme_system"),
        "light" to Localization.get(selectedLang, "theme_light"),
        "dark" to Localization.get(selectedLang, "theme_dark")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.get(selectedLang, "settings_title")) },
                navigationIcon = {
                    TextButton(onClick = {
                        preferencesManager.userName = userName.ifBlank { "DaniSalut" }
                        preferencesManager.language = selectedLang
                        preferencesManager.darkMode = selectedTheme
                        onSettingsChanged()
                        onNavigateBack()
                    }) {
                        Text(Localization.get(selectedLang, "back"))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // User Name
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text(Localization.get(selectedLang, "user_name_label")) },
                modifier = Modifier.fillMaxWidth()
            )

            // Language Selection
            Text(text = Localization.get(selectedLang, "language_label"), style = MaterialTheme.typography.titleMedium)
            
            languages.forEach { (code, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, modifier = Modifier.alignByBaseline())
                    RadioButton(
                        selected = selectedLang == code,
                        onClick = { selectedLang = code }
                    )
                }
            }

            HorizontalDivider()

            // Theme Selection
            Text(text = Localization.get(selectedLang, "theme_label"), style = MaterialTheme.typography.titleMedium)

            themes.forEach { (code, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, modifier = Modifier.alignByBaseline())
                    RadioButton(
                        selected = selectedTheme == code,
                        onClick = { selectedTheme = code }
                    )
                }
            }

            Button(
                onClick = {
                    preferencesManager.userName = userName.ifBlank { "DaniSalut" }
                    preferencesManager.language = selectedLang
                    preferencesManager.darkMode = selectedTheme
                    onSettingsChanged()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(Localization.get(selectedLang, "back"))
            }
        }
    }
}
