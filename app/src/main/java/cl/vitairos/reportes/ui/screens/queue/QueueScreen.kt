package cl.vitairos.reportes.ui.screens.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import cl.vitairos.reportes.domain.Session
import cl.vitairos.reportes.domain.UserRole
import cl.vitairos.reportes.ui.LocalOnline
import cl.vitairos.reportes.ui.components.BigOutlinedButton
import cl.vitairos.reportes.ui.components.EmptyState
import cl.vitairos.reportes.ui.components.MainBottomBar
import cl.vitairos.reportes.ui.components.ReportCard
import cl.vitairos.reportes.ui.components.ScreenScaffold
import cl.vitairos.reportes.ui.navigation.Routes
import cl.vitairos.reportes.ui.screens.ReportsUiState

@Composable
fun QueueScreen(
    session: Session,
    state: ReportsUiState,
    onSyncNow: () -> Unit,
    onRetry: (String) -> Unit,
    onOpenReport: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val queue = state.queue
    val esOperador = session.role == UserRole.OPERADOR
    ScreenScaffold(
        title = "Cola de envíos",
        onBack = { onNavigate(Routes.HOME) },
        bottomBar = { MainBottomBar(Routes.QUEUE, session.role, queue.size, onNavigate) }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    if (LocalOnline.current) "Los reportes se envían solos; también puedes forzarlo."
                    else "Sin conexión: se enviarán automáticamente al recuperar la red.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (esOperador) {
                item {
                    BigOutlinedButton(
                        text = "Sincronizar ahora",
                        onClick = onSyncNow,
                        icon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                    )
                }
            }
            if (queue.isEmpty()) {
                item { EmptyState("No hay reportes pendientes de envío.") }
            } else {
                items(queue, key = { it.report.id }) { details ->
                    ReportCard(
                        details = details,
                        onClick = { onOpenReport(details.report.id) },
                        onRetry = if (esOperador) ({ onRetry(details.report.id) }) else null
                    )
                }
            }
        }
    }
}
