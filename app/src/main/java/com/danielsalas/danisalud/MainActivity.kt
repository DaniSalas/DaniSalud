package com.danielsalas.danisalud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.danielsalas.danisalud.data.HealthDatabase
import com.danielsalas.danisalud.data.HealthRepository
import com.danielsalas.danisalud.data.PreferencesManager
import com.danielsalas.danisalud.ui.*
import com.danielsalas.danisalud.ui.theme.DaniSaludTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = HealthDatabase.getDatabase(applicationContext)
        val repository = HealthRepository(database.healthDao())
        val preferencesManager = PreferencesManager(applicationContext)

        setContent {
            var refreshKey by remember { mutableStateOf(0) }
            val currentLang = remember(refreshKey) { preferencesManager.language }
            val currentThemeMode = remember(refreshKey) { preferencesManager.darkMode }
            val currentUserName = remember(refreshKey) { preferencesManager.userName }

            val darkTheme = when (currentThemeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            DaniSaludStateWrapper(
                repository = repository,
                preferencesManager = preferencesManager,
                darkTheme = darkTheme,
                currentLang = currentLang,
                currentUserName = currentUserName,
                refreshKey = refreshKey
            ) {
                refreshKey++
            }
        }
    }
}

@Composable
fun DaniSaludStateWrapper(
    repository: HealthRepository,
    preferencesManager: PreferencesManager,
    darkTheme: Boolean,
    currentLang: String,
    currentUserName: String,
    refreshKey: Int,
    onSettingsChanged: () -> Unit
) {
    DaniSaludTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        val viewModel: HealthViewModel = viewModel(
            factory = HealthViewModelFactory(repository)
        )

        NavHost(navController = navController, startDestination = "dashboard") {
            composable("dashboard") {
                DashboardScreen(
                    onNavigateToAddEntry = { navController.navigate("add_entry") },
                    onNavigateToHistory = { navController.navigate("history") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    viewModel = viewModel,
                    userName = currentUserName,
                    language = currentLang
                )
            }
            composable("add_entry") {
                AddEntryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOcrWeight = { navController.navigate("ocr_weight") },
                    viewModel = viewModel,
                    language = currentLang
                )
            }
            composable("ocr_weight") {
                CameraOcrScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = viewModel,
                    language = currentLang,
                    mode = "weight"
                )
            }
            composable("history") {
                HistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = viewModel,
                    language = currentLang
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    preferencesManager = preferencesManager,
                    onSettingsChanged = onSettingsChanged
                )
            }
        }
    }
}
