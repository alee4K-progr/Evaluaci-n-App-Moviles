package com.corpaliv.albalab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = AzulAlba,
    onPrimary = Color.White,
    primaryContainer = AzulAlbaClaro,
    onPrimaryContainer = AzulAlbaOscuro,
    secondary = VerdeAlba,
    onSecondary = Color.White,
    secondaryContainer = VerdeAlbaClaro,
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = NaranjaAlba,
    onTertiary = Color.White,
    tertiaryContainer = NaranjaAlbaClaro,
    onTertiaryContainer = Color(0xFF3A1B00),
    background = FondoClaro,
    onBackground = Color(0xFF14202A),
    surface = Color.White,
    onSurface = Color(0xFF14202A),
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = TextoVariante,
    error = ErrorClaro,
    onError = Color.White
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulSuave,
    onPrimary = Color(0xFF00334F),
    primaryContainer = Color(0xFF004B75),
    onPrimaryContainer = AzulAlbaClaro,
    secondary = VerdeSuave,
    onSecondary = Color(0xFF00382F),
    secondaryContainer = Color(0xFF005046),
    onSecondaryContainer = VerdeAlbaClaro,
    tertiary = NaranjaSuave,
    onTertiary = Color(0xFF4B2500),
    tertiaryContainer = Color(0xFF6A3500),
    onTertiaryContainer = NaranjaAlbaClaro,
    background = FondoOscuro,
    onBackground = Color(0xFFE3ECF2),
    surface = SuperficieOscura,
    onSurface = Color(0xFFE3ECF2),
    surfaceVariant = SuperficieVarianteOscura,
    onSurfaceVariant = TextoVarianteOscuro,
    error = ErrorOscuro,
    onError = Color(0xFF601410)
)

@Composable
fun AlbaLabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}

/** Color para estados de éxito (no existe en Material 3 por defecto). */
@Composable
fun colorExito(): Color = if (isSystemInDarkTheme()) ExitoOscuro else ExitoClaro
