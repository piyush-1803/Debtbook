package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerIndigo,
    onPrimaryContainer = OnPrimaryContainerIndigo,
    secondary = SecondaryGreen,
    onSecondary = OnSecondaryWhite,
    secondaryContainer = SecondaryContainerGreen,
    onSecondaryContainer = OnSecondaryContainerGreen,
    tertiary = TertiaryRed,
    onTertiary = OnTertiaryWhite,
    error = ErrorRed,
    onError = OnErrorWhite,
    background = OnBackgroundColor,
    onBackground = BackgroundColor,
    surface = OnBackgroundColor,
    onSurface = BackgroundColor
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerIndigo,
    onPrimaryContainer = OnPrimaryContainerIndigo,
    secondary = SecondaryGreen,
    onSecondary = OnSecondaryWhite,
    secondaryContainer = SecondaryContainerGreen,
    onSecondaryContainer = OnSecondaryContainerGreen,
    tertiary = TertiaryRed,
    onTertiary = OnTertiaryWhite,
    error = ErrorRed,
    onError = OnErrorWhite,
    background = BackgroundColor,
    onBackground = OnBackgroundColor,
    surface = SurfaceColor,
    onSurface = OnSurfaceColor,
    surfaceVariant = SurfaceVariantColor,
    onSurfaceVariant = OnSurfaceVariantColor,
    outline = OutlineColor,
    outlineVariant = OutlineVariantColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic colors to enforce the specific branding requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        LightColorScheme // Force a clean clear aesthetic as requested, or support light/dark nicely
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
