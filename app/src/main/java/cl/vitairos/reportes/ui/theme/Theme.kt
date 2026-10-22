package cl.vitairos.reportes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Permite que cualquier componente sepa si el tema activo es oscuro (p. ej. los chips de estado). */
val LocalDarkTheme = staticCompositionLocalOf { false }

private val LightColors = lightColorScheme(
    primary = VerdeMineral,
    onPrimary = Hueso,
    primaryContainer = Niebla,
    onPrimaryContainer = VerdeNoche,
    secondary = CobreTexto,
    onSecondary = Hueso,
    secondaryContainer = Color(0xFFF6D5C8),
    onSecondaryContainer = Color(0xFF5A2410),
    tertiary = Color(0xFF7A5A12),
    onTertiary = Hueso,
    background = Hueso,
    onBackground = VerdeNoche,
    surface = Hueso,
    onSurface = VerdeNoche,
    surfaceVariant = Niebla,
    onSurfaceVariant = Color(0xFF3F5149),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Niebla,
    surfaceContainerHigh = Color(0xFFDDE7E0),
    surfaceContainerHighest = Color(0xFFD3DFD7),
    outline = Color(0xFF5E6F69),
    outlineVariant = Color(0xFFC9D3CD),
    error = Color(0xFFA63A18),
    onError = Color.White,
    errorContainer = Color(0xFFF6D5C8),
    onErrorContainer = Color(0xFF7A2E12)
)

private val DarkColors = darkColorScheme(
    primary = Cobre,
    onPrimary = VerdeNoche,
    primaryContainer = VerdeMineral,
    onPrimaryContainer = Hueso,
    secondary = Oro,
    onSecondary = VerdeNoche,
    secondaryContainer = Color(0xFF6B3218),
    onSecondaryContainer = Color(0xFFF6D5C8),
    tertiary = Oro,
    onTertiary = VerdeNoche,
    background = VerdeNoche,
    onBackground = Hueso,
    surface = VerdeNoche,
    onSurface = Hueso,
    surfaceVariant = VerdeMineral,
    onSurfaceVariant = Acero,
    surfaceContainerLowest = Color(0xFF06150F),
    surfaceContainerLow = VerdeProfundo,
    surfaceContainer = VerdeProfundo,
    surfaceContainerHigh = VerdeMineral,
    surfaceContainerHighest = Color(0xFF22403A),
    outline = Color(0xFF7F9189),
    outlineVariant = Color(0xFF2A4A40),
    error = Color(0xFFF2A58A),
    onError = Color(0xFF3A1204),
    errorContainer = Color(0xFF7A2E12),
    onErrorContainer = Color(0xFFF6D5C8)
)

@Composable
fun VitairosTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            content = content
        )
    }
}
