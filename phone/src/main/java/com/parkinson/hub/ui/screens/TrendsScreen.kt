package com.parkinson.hub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parkinson.hub.ui.theme.Coral600
import com.parkinson.hub.ui.theme.Emerald600
import com.parkinson.hub.ui.theme.Indigo700
import com.parkinson.hub.ui.viewmodel.TrendsViewModel

@Composable
fun TrendsScreen(
    viewModel: TrendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tendencias",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Análisis de los últimos 7 días",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        item {
            TrendCard(
                title = "Motor",
                subtitle = "Temblor, marcha y rigidez",
                icon = Icons.Default.Timeline,
                color = Indigo700,
                trendValue = uiState.motorTrend,
                trendDescription = uiState.motorTrendDescription,
                chartData = uiState.motorData
            )
        }

        item {
            TrendCard(
                title = "Cardio-Autonómico",
                subtitle = "FC, HRV y variabilidad",
                icon = Icons.Default.Favorite,
                color = Coral600,
                trendValue = uiState.cardioTrend,
                trendDescription = uiState.cardioTrendDescription,
                chartData = uiState.cardioData
            )
        }

        item {
            TrendCard(
                title = "Sueño",
                subtitle = "Calidad y fases del sueño",
                icon = Icons.Default.NightsStay,
                color = Emerald600,
                trendValue = uiState.sleepTrend,
                trendDescription = uiState.sleepTrendDescription,
                chartData = uiState.sleepData
            )
        }

        item {
            TrendCard(
                title = "Ejercicio",
                subtitle = "Actividad física y carga",
                icon = Icons.Default.Sports,
                color = Indigo700,
                trendValue = uiState.exerciseTrend,
                trendDescription = uiState.exerciseTrendDescription,
                chartData = uiState.exerciseData
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun TrendCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    trendValue: String,
    trendDescription: String,
    chartData: List<Float>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = trendValue,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = trendDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                chartData.forEachIndexed { index, value ->
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .height(60.dp)
                                .fillMaxWidth(0.1f)
                        ) {
                            val maxValue = chartData.maxOrNull() ?: 1f
                            val barHeight = (value / maxValue) * size.height
                            drawRoundRect(
                                color = color.copy(alpha = 0.6f),
                                size = androidx.compose.ui.geometry.Size(
                                    width = size.width * 0.8f,
                                    height = barHeight
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                            )
                        }
                        Text(
                            text = listOf("L", "M", "X", "J", "V", "S", "D").getOrElse(index) { "" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
