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

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = MintPrimary,
    onPrimary = Color(0xFF00201A),
    background = TextDark,
    onBackground = Color.White,
    surface = Color(0xFF1E293B),
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF94A3B8)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.White,
    primaryContainer = MintPrimary,
    onPrimaryContainer = Color(0xFF00221A),
    secondary = LavenderSoft,
    onSecondary = Color.White,
    tertiary = SkyBlue,
    background = CreamBg,
    onBackground = TextDark,
    surface = SurfaceWhite,
    onSurface = TextDark,
    onSurfaceVariant = TextMuted,
    error = ErrorColor,
    errorContainer = ErrorContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // For a unified high-fidelity nursery look, preserve custom theme colors by default
  dynamicColor: Boolean = false,
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
