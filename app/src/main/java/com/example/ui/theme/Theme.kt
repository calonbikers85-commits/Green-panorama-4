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

private val DarkColorScheme =
  darkColorScheme(
    primary = GreenPanoramaDarkPrimary,
    secondary = GreenPanoramaDarkSecondary,
    tertiary = GreenPanoramaTertiary,
    background = GreenPanoramaDarkBackground,
    surface = GreenPanoramaDarkSurface,
    error = EmergencyRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GreenPanoramaPrimary,
    secondary = GreenPanoramaSecondary,
    tertiary = GreenPanoramaTertiary,
    background = GreenPanoramaBackground,
    surface = GreenPanoramaSurface,
    surfaceVariant = GreenPanoramaSurfaceVariant,
    onPrimary = GreenPanoramaOnPrimary,
    onSecondary = GreenPanoramaOnSecondary,
    onBackground = GreenPanoramaOnSurface,
    onSurface = GreenPanoramaOnSurface,
    onSurfaceVariant = GreenPanoramaOnSurfaceVariant,
    error = EmergencyRed
  )

@Composable
fun GreenPanoramaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our brand colors by default
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) = GreenPanoramaTheme(darkTheme, dynamicColor, content)
