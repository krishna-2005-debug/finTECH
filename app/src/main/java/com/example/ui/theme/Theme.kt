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
    primary = FintechPurple,
    secondary = FintechSecondary,
    tertiary = FintechAccent,
    background = DarkBg,
    surface = CardBgDark,
    onPrimary = LightBg,
    onSecondary = LightBg,
    onTertiary = DarkBg,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardBgDark,
    onSurfaceVariant = TextSecondaryDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = FintechPurple,
    secondary = FintechSecondary,
    tertiary = FintechAccent,
    background = LightBg,
    surface = CardBgLight,
    onPrimary = LightBg,
    onSecondary = LightBg,
    onTertiary = DarkBg,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = CardBgLight,
    onSurfaceVariant = TextSecondaryLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to enforce brand colors
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
