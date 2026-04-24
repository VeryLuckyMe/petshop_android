package com.example.zootopia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

import androidx.compose.ui.graphics.Color

val BrandDark = Color(0xFF1B3C53)
val BrandMedium = Color(0xFF456882)
val ZootopiaPrimary = Color(0xFFFF8C31)
val BackgroundLight = Color(0xFFF8F6F6)

private val DarkColorScheme = darkColorScheme(
    primary = ZootopiaPrimary,
    secondary = BrandMedium,
    background = BrandDark,
    surface = BrandDark
)

private val LightColorScheme = lightColorScheme(
    primary = ZootopiaPrimary,
    secondary = BrandMedium,
    background = BackgroundLight,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BrandDark,
    onSurface = BrandDark
)

@Composable
fun AndroidstuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
