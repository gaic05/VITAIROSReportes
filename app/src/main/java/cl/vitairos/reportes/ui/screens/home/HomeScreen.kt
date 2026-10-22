package cl.vitairos.reportes.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.vitairos.reportes.domain.Session
import cl.vitairos.reportes.domain.UserRole
import cl.vitairos.reportes.ui.components.BigButton
import cl.vitairos.reportes.ui.components.EmptyState
import cl.vitairos.reportes.ui.components.MainBottomBar
import cl.vitairos.reportes.ui.components.ReportCard
import cl.vitairos.reportes.ui.components.ScreenScaffold
import cl.vitairos.reportes.ui.components.SectionLabel
import cl.vitairos.reportes.ui.navigation.Routes
import cl.vitairos.reportes.ui.screens.ReportsUiState
import androidx.compose.material.icons.filled.Person

@Composable
fun HomeScreen(
    session: Session,
    state: ReportsUiState,
    onNewReport: () -> Unit,
    onOpenReport: (String) -> Unit,
    onOpenQueue: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigate: (String) -> Unit
) {
    ScreenScaffold(
        title = "Inicio",
        bottomBar = { MainBottomBar(Routes.HOME, session.role, state.queue.size, onNavigate) },
        actions = {
            IconButton(onClick = onOpenProfile) {
                Icon(Icons.Default.Person, contentDescription = "Mi perfil")
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Ajustes")
            }
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text("Hola, ${session.nombre}", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Perfil ${session.role.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item { SectionLabel("Resumen") }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CounterCard("Borradores", state.countBorradores, onOpenHistory, Modifier.weight(1f))
                    CounterCard("En cola", state.countEnCola, onOpenQueue, Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CounterCard("Con error", state.countConError, onOpenQueue, Modifier.weight(1f))
                    CounterCard("Sincronizados", state.countSincronizados, onOpenHistory, Modifier.weight(1f))
                }
            }
            if (session.role == UserRole.OPERADOR) {
                item {
                    BigButton(
                        text = "Nuevo reporte",
                        onClick = onNewReport,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) }
                    )
                }
            }
            item { SectionLabel("Reportes recientes", Modifier.padding(top = 4.dp)) }
            if (state.recent.isEmpty()) {
                item {
                    EmptyState(
                        if (session.role == UserRole.OPERADOR) "Aún no hay reportes. Crea el primero con «Nuevo reporte»."
                        else "Aún no hay reportes registrados en este dispositivo."
                    )
                }
            } else {
                items(state.recent, key = { it.report.id }) { details ->
                    ReportCard(details = details, onClick = { onOpenReport(details.report.id) })
                }
            }
        }
    }
}

@Composable
private fun CounterCard(label: String, count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(count.toString(), style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
