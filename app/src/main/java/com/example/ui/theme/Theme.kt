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

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFF9ECAFF),
  onPrimary = Color(0xFF003258),
  primaryContainer = Color(0xFF00497D),
  onPrimaryContainer = SleekBlueContainer,
  secondary = Color(0xFFBAC8DB),
  onSecondary = Color(0xFF243140),
  secondaryContainer = Color(0xFF3B4858),
  onSecondaryContainer = Color(0xFFD6E4F7),
  tertiary = SunYellow,
  background = DarkSurface,
  surface = Color(0xFF1A1C1E),
  onBackground = Color(0xFFE1E2EC),
  onSurface = Color(0xFFE1E2EC),
  surfaceVariant = Color(0xFF44474E),
  onSurfaceVariant = Color(0xFFC4C6D0),
  error = SleekAlertRed
)

private val LightColorScheme = lightColorScheme(
  primary = SleekBluePrimary,
  onPrimary = Color.White,
  primaryContainer = SleekBlueContainer,
  onPrimaryContainer = SleekDeepInk,
  secondary = SleekTextSecondary,
  onSecondary = Color.White,
  secondaryContainer = SleekCardBg,
  onSecondaryContainer = SleekDeepInk,
  tertiary = SunYellow,
  background = SleekBackground,
  surface = CardBackground,
  onBackground = SleekTextPrimary,
  onSurface = SleekTextPrimary,
  surfaceVariant = SleekCardBg,
  onSurfaceVariant = SleekTextSecondary,
  error = SleekAlertRed
)

@Composable
fun WeatherGptTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep branded high-contrast weather colors
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) = WeatherGptTheme(darkTheme, dynamicColor, content)
