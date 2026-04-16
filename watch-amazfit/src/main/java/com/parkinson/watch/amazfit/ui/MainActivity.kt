package com.parkinson.watch.amazfit.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.parkinson.watch.amazfit.ui.screens.DashboardScreen
import com.parkinson.watch.amazfit.ui.screens.MonitoringScreen
import com.parkinson.watch.amazfit.ui.screens.SettingsScreen
import com.parkinson.watch.amazfit.ui.screens.StatusScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ParkinsONAmazfitApp()
        }
    }
}

@Composable
fun ParkinsONAmazfitApp() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    onNavigateToMonitoring = { navController.navigate("monitoring") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToStatus = { navController.navigate("status") }
                )
            }
            composable("monitoring") {
                MonitoringScreen(onBack = { navController.popBackStack() })
            }
            composable("settings") {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable("status") {
                StatusScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
