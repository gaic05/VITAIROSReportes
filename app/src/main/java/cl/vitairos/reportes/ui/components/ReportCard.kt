package cl.vitairos.reportes.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.vitairos.reportes.data.local.AttachmentEntity
import cl.vitairos.reportes.data.local.ReportWithDetails
import cl.vitairos.reportes.domain.ReportStatus
import cl.vitairos.reportes.ui.formatDateTime

/** Referencia breve del documento: nombre del primer adjunto (+ cuántos más). */
fun attachmentRef(attachments: List<AttachmentEntity>): String = when {
    attachments.isEmpty() -> "sin adjuntos"
    attachments.size == 1 -> attachments.first().nombre
    else -> "${attachments.first().nombre} +${attachments.size - 1}"
}

/**
 * Tarjeta de reporte. Muestra lo mínimo exigido en el historial: identificador, tipo, proceso,
 * fecha/hora, usuario, estado y referencia del documento.
 */
@Composable
fun ReportCard(
    details: ReportWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    val r = details.report
    val esError = r.estado == ReportStatus.ERROR || r.estado == ReportStatus.REQUIERE_CORRECCION
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(r.id, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                StatusChip(r.estado)
            }
            Text(r.tipo, style = MaterialTheme.typography.titleMedium)
            Text(
                "${r.proceso} · ${formatDateTime(r.creadoEn)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${r.usuario} · ${attachmentRef(details.attachments)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (r.mensaje.isNotBlank()) {
                Text(
                    r.mensaje,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onRetry != null && r.estado == ReportStatus.ERROR) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Text("  Reintentar envío")
                }
            }
        }
    }
}
