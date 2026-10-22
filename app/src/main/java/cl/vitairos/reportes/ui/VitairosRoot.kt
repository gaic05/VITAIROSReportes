package cl.vitairos.reportes.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.vitairos.reportes.di.AppContainer
import cl.vitairos.reportes.domain.ThemeMode
import cl.vitairos.reportes.ui.navigation.AppNavHost
import cl.vitairos.reportes.ui.screens.AppViewModel
import cl.vitairos.reportes.ui.theme.VitairosTheme
import kotlinx.coroutines.flow.collectLatest

/** Raíz de la UI: tema claro/oscuro, mensajes globales, estado de conexión y navegación. */
@Composable
fun VitairosRoot(container: AppContainer, applyEdgeToEdge: (isDark: Boolean) -> Unit) {
    val appViewModel: AppViewModel = viewModel(factory = AppViewModel.factory(container))
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    val online by appViewModel.isOnline.collectAsStateWithLifecycle()

    val systemDark = isSystemInDarkTheme()
    val current = settings
    // Si el usuario nunca tocó el botón, se sigue el tema del sistema.
    val isDark = when (current?.themeMode) {
        ThemeMode.CLARO -> false
        ThemeMode.OSCURO -> true
        else -> systemDark
    }

    DisposableEffect(isDark) {
        applyEdgeToEdge(isDark)
        onDispose { }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        appViewModel.messages.collectLatest { message -> snackbarHostState.showSnackbar(message) }
    }

    VitairosTheme(darkTheme = isDark) {
        CompositionLocalProvider(
            LocalThemeController provides ThemeController(isDark = isDark, toggle = { appViewModel.toggleTheme(isDark) }),
            LocalSnackbarHost provides snackbarHostState,
            LocalOnline provides online
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                // Se espera a que DataStore cargue para no mostrar el login un instante si ya hay sesión.
                if (current != null) {
                    AppNavHost(container = container, appViewModel = appViewModel, settings = current)
                }
            }
        }
    }
}
