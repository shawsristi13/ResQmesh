package com.meshmap.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MeshMapColorScheme = darkColorScheme(
    primary = IcyBlue,
    onPrimary = DeepNavy,
    primaryContainer = IcyBlueSurface,
    onPrimaryContainer = IcyBlue,
    secondary = SafetyGreen,
    onSecondary = DeepNavy,
    secondaryContainer = SafetyGreenSurface,
    onSecondaryContainer = SafetyGreen,
    tertiary = WarningAmber,
    onTertiary = DeepNavy,
    tertiaryContainer = WarningAmberSurface,
    onTertiaryContainer = WarningAmber,
    error = AlertRed,
    onError = DeepNavy,
    errorContainer = AlertRedSurface,
    onErrorContainer = AlertRed,
    background = DeepNavy,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    outline = TextDim
)

@Composable
fun MeshMapTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MeshMapColorScheme,
        typography = MeshMapTypography,
        content = content
    )
}
