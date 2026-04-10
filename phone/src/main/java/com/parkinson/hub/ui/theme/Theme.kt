package com.parkinson.hub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Indigo200,
    onPrimary = Color.White,
    primaryContainer = Indigo700,
    onPrimaryContainer = Color.White,
    secondary = Emerald400,
    onSecondary = Color.Black,
    secondaryContainer = Emerald800,
    onSecondaryContainer = Color.White,
    tertiary = Amber400,
    onTertiary = Color.Black,
    tertiaryContainer = Amber600,
    onTertiaryContainer = Color.Black,
    error = Coral400,
    onError = Color.Black,
    errorContainer = Coral600,
    onErrorContainer = Color.White,
    background = Gray900,
    onBackground = Gray100,
    surface = Gray800,
    onSurface = Gray100,
    surfaceVariant = Gray700,
    onSurfaceVariant = Gray300
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo700,
    onPrimary = Color.White,
    primaryContainer = Indigo200,
    onPrimaryContainer = Indigo900,
    secondary = Emerald800,
    onSecondary = Color.White,
    secondaryContainer = Emerald400,
    onSecondaryContainer = Color.Black,
    tertiary = Amber600,
    onTertiary = Color.Black,
    tertiaryContainer = Amber400,
    onTertiaryContainer = Color.Black,
    error = Coral600,
    onError = Color.White,
    errorContainer = Coral400,
    onErrorContainer = Color.Black,
    background = Gray50,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700
)

@Composable
fun ParkinsONHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
