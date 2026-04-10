package com.parkinson.watch.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.parkinson.watch.ui.screens.DashboardScreen
import com.parkinson.watch.ui.screens.MedicationScreen
import com.parkinson.watch.ui.screens.MonitoringScreen
import com.parkinson.watch.ui.screens.QuickLogScreen
import com.parkinson.watch.ui.screens.SettingsScreen
import com.parkinson.watch.ui.screens.StatusScreen
import com.parkinson.watch.ui.theme.ParkinsONWatchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParkinsONWatchTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WatchNavigation()
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    data object Status : Screen("status")
    data object Dashboard : Screen("dashboard")
    data object Monitoring : Screen("monitoring")
    data object QuickLog : Screen("quick_log")
    data object Medication : Screen("medication")
    data object Settings : Screen("settings")
}

@Composable
fun WatchNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Status.route
    ) {
        composable(Screen.Status.route) {
            StatusScreen(
                onNavigateToDashboard = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToQuickLog = { navController.navigate(Screen.QuickLog.route) }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMonitoring = { navController.navigate(Screen.Monitoring.route) }
            )
        }
        composable(Screen.Monitoring.route) {
            MonitoringScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.QuickLog.route) {
            QuickLogScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMedication = { navController.navigate(Screen.Medication.route) }
            )
        }
        composable(Screen.Medication.route) {
            MedicationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
