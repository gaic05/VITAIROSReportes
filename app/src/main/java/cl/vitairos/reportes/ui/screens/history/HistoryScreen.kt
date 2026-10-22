package cl.vitairos.reportes.ui.screens.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.vitairos.reportes.domain.Session
import cl.vitairos.reportes.ui.components.EmptyState
import cl.vitairos.reportes.ui.components.MainBottomBar
import cl.vitairos.reportes.ui.components.ReportCard
import cl.vitairos.reportes.ui.components.ScreenScaffold
import cl.vitairos.reportes.ui.navigation.Routes
import cl.vitairos.reportes.ui.screens.HistoryFilter
import cl.vitairos.reportes.ui.screens.ReportsUiState

@Composable
fun HistoryScreen(
    session: Session,
    state: ReportsUiState,
    onFilter: (HistoryFilter) -> Unit,
    onOpenReport: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    ScreenScaffold(
        title = "Historial",
        onBack = { onNavigate(Routes.HOME) },
        bottomBar = { MainBottomBar(Routes.HISTORY, session.role, state.queue.size, onNavigate) }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HistoryFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = state.filter == filter,
                            onClick = { onFilter(filter) },
                            label = { Text(filter.label) }
                        )
                    }
                }
            }
            val lista = state.filtered
            if (lista.isEmpty()) {
                item { EmptyState("No hay reportes para este filtro.") }
            } else {
                items(lista, key = { it.report.id }) { details ->
                    ReportCard(details = details, onClick = { onOpenReport(details.report.id) })
                }
            }
        }
    }
}
