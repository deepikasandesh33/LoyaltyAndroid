package com.loyaltyapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Indigo = Color(0xFF5856D6)
val IndigoLight = Color(0xFF7B79E8)
val Purple = Color(0xFFAF52DE)

private val ColorScheme = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    secondary = Purple,
    onSecondary = Color.White,
    background = Color(0xFFF2F2F7),
    surface = Color.White,
    onBackground = Color(0xFF1C1C1E),
    onSurface = Color(0xFF1C1C1E),
)

@Composable
fun LoyaltyAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content
    )
}
