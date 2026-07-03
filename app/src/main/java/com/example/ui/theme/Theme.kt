package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EditorialPrimaryDark,
    secondary = EditorialSecondaryDark,
    tertiary = EditorialTertiaryDark,
    background = EditorialBackgroundDark,
    surface = EditorialSurfaceDark,
    onBackground = EditorialOnBackgroundDark,
    onSurface = EditorialOnSurfaceDark,
    surfaceVariant = EditorialSurfaceVariantDark,
    primaryContainer = EditorialPrimaryContainerDark,
    secondaryContainer = EditorialSecondaryContainerDark,
)

private val LightColorScheme = lightColorScheme(
    primary = EditorialPrimary,
    secondary = EditorialSecondary,
    tertiary = EditorialTertiary,
    background = EditorialBackground,
    surface = EditorialSurface,
    onBackground = EditorialOnBackground,
    onSurface = EditorialOnSurface,
    surfaceVariant = EditorialSurfaceVariant,
    outline = EditorialOutline,
    primaryContainer = EditorialPrimaryContainer,
    onPrimaryContainer = EditorialOnPrimaryContainer,
    secondaryContainer = EditorialSecondaryContainer,
    onSecondaryContainer = EditorialOnSecondaryContainer,
    errorContainer = EditorialErrorContainer,
    onErrorContainer = EditorialOnErrorContainer,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
