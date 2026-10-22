package cl.vitairos.reportes.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import cl.vitairos.reportes.R
import cl.vitairos.reportes.domain.UserRole
import cl.vitairos.reportes.ui.LocalOnline
import cl.vitairos.reportes.ui.LocalSnackbarHost
import cl.vitairos.reportes.ui.LocalThemeController
import cl.vitairos.reportes.ui.navigation.Routes
import cl.vitairos.reportes.ui.theme.Hueso
import cl.vitairos.reportes.ui.theme.Oro
import cl.vitairos.reportes.ui.theme.VerdeNoche

/** Botón claro/oscuro: el ícono cambia con una transición suave. */
@Composable
fun ThemeToggleButton(isDark: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Crossfade(targetState = isDark, label = "icono-tema") { dark ->
            Icon(
                painter = painterResource(if (dark) R.drawable.ic_sun else R.drawable.ic_moon),
                contentDescription = if (dark) "Cambiar a modo claro" else "Cambiar a modo oscuro"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitairosTopBar(
    title: String,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val theme = LocalThemeController.current
    TopAppBar(
        title = {
            Column {
                Text("VITAIROS", style = MaterialTheme.typography.labelSmall, color = Oro)
                Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = {
            actions()
            ThemeToggleButton(isDark = theme.isDark, onToggle = theme.toggle)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = VerdeNoche,
            titleContentColor = Hueso,
            navigationIconContentColor = Hueso,
            actionIconContentColor = Hueso
        )
    )
}

/**
 * Estructura común de todas las pantallas: barra superior de marca, aviso de conexión,
 * mensajes (Snackbar) y barra inferior opcional.
 */
@Composable
fun ScreenScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    val snackbarHostState = LocalSnackbarHost.current
    Scaffold(
        topBar = { VitairosTopBar(title = title, onBack = onBack, actions = actions) },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ConnectionBanner(online = LocalOnline.current)
            Box(Modifier.weight(1f).fillMaxSize()) { content() }
        }
    }
}

@Composable
fun MainBottomBar(
    current: String,
    role: UserRole,
    queueCount: Int,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = current == Routes.HOME,
            onClick = { onNavigate(Routes.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Inicio") }
        )
        if (role == UserRole.OPERADOR) {
            NavigationBarItem(
                selected = false,
                onClick = { onNavigate(Routes.NEW) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                label = { Text("Nuevo") }
            )
        }
        NavigationBarItem(
            selected = current == Routes.QUEUE,
            onClick = { onNavigate(Routes.QUEUE) },
            icon = {
                BadgedBox(badge = { if (queueCount > 0) Badge { Text(queueCount.toString()) } }) {
                    Icon(painterResource(R.drawable.ic_clock), contentDescription = null)
                }
            },
            label = { Text("Cola") }
        )
        NavigationBarItem(
            selected = current == Routes.HISTORY,
            onClick = { onNavigate(Routes.HISTORY) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text("Historial") }
        )
    }
}
