package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF4DD0E1),
    tertiary = Color(0xFFFFB74D),
    background = Color(0xFF0B131E),
    surface = Color(0xFF131D2A),
    surfaceVariant = Color(0xFF1E2B3D),
    outline = Color(0xFF33475E)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MpkNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = MpkNavyLight,
    onPrimaryContainer = MpkNavyDark,
    secondary = MpkTealSecondary,
    onSecondary = Color.White,
    secondaryContainer = MpkTealContainer,
    tertiary = MpkAmberAccent,
    tertiaryContainer = MpkAmberContainer,
    background = MpkBackground,
    surface = MpkSurface,
    surfaceVariant = MpkSurfaceVariant,
    outline = MpkBorder,
    onBackground = MpkTextPrimary,
    onSurface = MpkTextPrimary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
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
