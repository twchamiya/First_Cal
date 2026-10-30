package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = Slate950,
    primaryContainer = CyanNeonContainer,
    onPrimaryContainer = CyanNeon,
    secondary = VioletElectric,
    onSecondary = Slate950,
    secondaryContainer = VioletContainer,
    onSecondaryContainer = VioletElectric,
    tertiary = EmeraldBright,
    onTertiary = Slate950,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate850,
    onSurfaceVariant = Slate200,
    outline = Slate700,
    outlineVariant = Slate800,
    error = RosePink,
    onError = Slate950
)

private val LightColorScheme = darkColorScheme(
    // Scientific graph calculators operate best in high-contrast deep slate
    primary = CyanNeon,
    onPrimary = Slate950,
    primaryContainer = CyanNeonContainer,
    onPrimaryContainer = CyanNeon,
    secondary = VioletElectric,
    onSecondary = Slate950,
    secondaryContainer = VioletContainer,
    onSecondaryContainer = VioletElectric,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate850,
    onSurfaceVariant = Slate200,
    outline = Slate700,
    error = RosePink,
    onError = Slate950
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek high-contrast dark theme for graphing & math
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Slate950.toArgb()
                window.navigationBarColor = Slate950.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
