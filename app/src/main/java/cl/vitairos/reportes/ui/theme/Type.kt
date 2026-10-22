package cl.vitairos.reportes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Tipografía nativa de Android (Roboto): VITAIROS no exige una tipografía corporativa y pide
 * priorizar la legibilidad. La fuente monoespaciada se usa solo en etiquetas técnicas (ids, kickers).
 */
private val base = Typography()

val AppTypography = Typography(
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelSmall = base.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
)
