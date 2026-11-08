package com.example.nicagrowmovil_v2

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColoresNicaGrow = lightColorScheme(
    primary = AzulNicaGrow,
    onPrimary = Color.White,
    primaryContainer = CelesteNicaGrow,
    onPrimaryContainer = NegroNicaGrow,
    secondary = AmarilloNicaGrow,
    onSecondary = TextoNicaGrow,
    secondaryContainer = AmarilloNicaGrow,
    onSecondaryContainer = TextoNicaGrow,
    tertiary = NegroNicaGrow,
    onTertiary = Color.White,
    background = FondoNicaGrow,
    onBackground = TextoNicaGrow,
    surface = Color.White,
    onSurface = TextoNicaGrow,
    surfaceVariant = CelesteNicaGrow,
    onSurfaceVariant = TextoNicaGrow,
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = CelesteNicaGrow,
    surfaceContainerHighest = CelesteNicaGrow,
    surfaceTint = AzulNicaGrow,
    outline = NegroNicaGrow.copy(alpha = 0.45f)
)

@Composable
fun NicaGrowMovil_v2Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColoresNicaGrow,

        content = content
    )
}


