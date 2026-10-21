package com.example.jieqi.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Cinnabar,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6DED2),
    onPrimaryContainer = Color(0xFF3B120A),
    secondary = BambooGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E9E1),
    onSecondaryContainer = Color(0xFF10291F),
    tertiary = Amber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF8E7C4),
    onTertiaryContainer = Color(0xFF3A2606),
    background = RiceWhite,
    onBackground = InkBlack,
    surface = Color(0xFFFFFBF5),
    onSurface = InkBlack,
    surfaceVariant = Color(0xFFEFE5D8),
    onSurfaceVariant = Color(0xFF57493C),
    outline = Color(0xFFA79785),
    outlineVariant = Color(0xFFDCCFBE)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE7A08C),
    onPrimary = Color(0xFF4A160C),
    primaryContainer = Color(0xFF6B2A1C),
    onPrimaryContainer = Color(0xFFFBDDD2),
    secondary = Color(0xFF9CC8B6),
    onSecondary = Color(0xFF10291F),
    secondaryContainer = Color(0xFF2C4C40),
    onSecondaryContainer = Color(0xFFD7E9E1),
    tertiary = Color(0xFFE8C479),
    onTertiary = Color(0xFF3A2606),
    tertiaryContainer = Color(0xFF5A431A),
    onTertiaryContainer = Color(0xFFF8E7C4),
    background = DarkInk,
    onBackground = Color(0xFFF0E7DA),
    surface = Color(0xFF211B16),
    onSurface = Color(0xFFF0E7DA),
    surfaceVariant = Color(0xFF3A322A),
    onSurfaceVariant = Color(0xFFD6C8B8),
    outline = Color(0xFF9C8C7A),
    outlineVariant = Color(0xFF4C4238)
)

/**
 * 应用主题。
 *
 * @param darkTheme 是否使用深色主题，默认跟随系统。
 * @param dynamicColor 是否启用 Android 12+ 动态取色（本项目默认关闭，以保持传统文化配色）。
 */
@Composable
fun JieQiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = JieQiTypography,
        content = content
    )
}
