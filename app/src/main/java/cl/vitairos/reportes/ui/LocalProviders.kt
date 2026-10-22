package cl.vitairos.reportes.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

/** Estado del tema y acción para cambiarlo (botón claro/oscuro disponible en todas las pantallas). */
data class ThemeController(val isDark: Boolean, val toggle: () -> Unit)

val LocalThemeController = staticCompositionLocalOf { ThemeController(isDark = false, toggle = {}) }

/** Mensajes dentro de la app (enviado, sin conexión, error…). */
val LocalSnackbarHost = staticCompositionLocalOf<SnackbarHostState> { error("SnackbarHostState no provisto") }

/** Conectividad actual (real + simulada). */
val LocalOnline = compositionLocalOf { true }
