package cl.vitairos.reportes.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.vitairos.reportes.domain.ReportRules
import cl.vitairos.reportes.domain.ReportStatus
import cl.vitairos.reportes.domain.Session
import cl.vitairos.reportes.ui.components.AttachmentThumb
import cl.vitairos.reportes.ui.components.BigButton
import cl.vitairos.reportes.ui.components.BigOutlinedButton
import cl.vitairos.reportes.ui.components.ConfirmDialog
import cl.vitairos.reportes.ui.components.KeyValueRow
import cl.vitairos.reportes.ui.components.ScreenScaffold
import cl.vitairos.reportes.ui.components.SectionLabel
import cl.vitairos.reportes.ui.components.StatusChip
import cl.vitairos.reportes.ui.components.SurfaceCard
import cl.vitairos.reportes.ui.formatDateTime
import cl.vitairos.reportes.ui.formatSize
import cl.vitairos.reportes.ui.formatTime

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    session: Session,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }

    ScreenScaffold(title = "Detalle del reporte", onBack = onBack) {
        val details = state.details
        if (details == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (!state.deleted) CircularProgressIndicator()
            }
        } else {
            val r = details.report
            val role = session.role
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SurfaceCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(r.id, style = MaterialTheme.typography.titleMedium)
                        StatusChip(r.estado)
                    }
                    Text(r.tipo, style = MaterialTheme.typography.bodyLarge)
                    if (r.mensaje.isNotBlank()) {
                        val esError = r.estado == ReportStatus.ERROR || r.estado == ReportStatus.REQUIERE_CORRECCION
                        Text(
                            r.mensaje,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SectionLabel("Datos del reporte")
                SurfaceCard {
                    KeyValueRow("Tipo", r.tipo)
                    KeyValueRow("Proceso", r.proceso)
                    KeyValueRow("Área / faena", r.area)
                    KeyValueRow("Turno", r.turno.ifBlank { "—" })
                    KeyValueRow("Usuario", r.usuario)
                    KeyValueRow("Fecha y hora", formatDateTime(r.creadoEn))
                    KeyValueRow("Observaciones", r.observaciones.ifBlank { "—" })
                    KeyValueRow("Versión", r.version.toString())
                    KeyValueRow("Intentos de envío", r.intentos.toString())
                    r.sincronizadoEn?.let { KeyValueRow("Sincronizado", formatDateTime(it)) }
                }

                SectionLabel("Adjuntos")
                SurfaceCard {
                    if (details.attachments.isEmpty()) {
                        Text("Sin adjuntos", style = MaterialTheme.typography.bodyMedium)
                    }
                    details.attachments.forEach { a ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AttachmentThumb(path = a.rutaLocal, mime = a.tipoMime, size = 48.dp)
                            Column {
                                Text(a.nombre, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    formatSize(a.tamanoBytes) + if (a.rutaLocal == null) " · copia local eliminada tras sincronizar" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                SectionLabel("Trazabilidad")
                SurfaceCard {
                    details.log.forEach { entry ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                formatTime(entry.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(entry.texto, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (ReportRules.canRetry(role, r.estado)) {
                    BigButton(
                        text = "Reintentar envío",
                        onClick = viewModel::retry,
                        icon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                    )
                }
                if (ReportRules.canEdit(role, r.estado)) {
                    BigOutlinedButton(
                        text = ReportRules.editLabel(r.estado),
                        onClick = { onEdit(r.id) },
                        icon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                }
                if (ReportRules.canObserve(role, r.estado)) {
                    BigOutlinedButton(
                        text = "Marcar como «Requiere corrección»",
                        onClick = viewModel::markObserved
                    )
                }
                if (ReportRules.canDelete(role, r.estado)) {
                    BigOutlinedButton(
                        text = "Eliminar reporte",
                        onClick = viewModel::askDelete,
                        icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }

            if (state.confirmDelete) {
                ConfirmDialog(
                    title = "¿Eliminar este reporte?",
                    text = "${r.id} se eliminará del teléfono junto con sus adjuntos. Esta acción no se puede deshacer.",
                    confirmLabel = "Eliminar",
                    onConfirm = viewModel::confirmDelete,
                    onDismiss = viewModel::cancelDelete
                )
            }
        }
    }
}
