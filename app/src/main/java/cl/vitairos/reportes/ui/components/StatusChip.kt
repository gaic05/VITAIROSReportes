package cl.vitairos.reportes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.vitairos.reportes.R
import cl.vitairos.reportes.domain.ReportStatus
import cl.vitairos.reportes.ui.theme.LocalDarkTheme

private data class StatusColors(val container: Color, val content: Color)

@Composable
private fun colorsFor(status: ReportStatus): StatusColors {
    val dark = LocalDarkTheme.current
    return when (status) {
        ReportStatus.BORRADOR ->
            if (dark) StatusColors(Color(0xFF1B342D), Color(0xFFE8EFE9)) else StatusColors(Color(0xFFE8EFE9), Color(0xFF1B342D))
        ReportStatus.EN_COLA ->
            if (dark) StatusColors(Color(0xFF4D3A08), Color(0xFFF3E3BF)) else StatusColors(Color(0xFFF3E3BF), Color(0xFF4D3A08))
        ReportStatus.ENVIANDO ->
            if (dark) StatusColors(Color(0xFF14463A), Color(0xFFD6E4DC)) else StatusColors(Color(0xFFD6E4DC), Color(0xFF102B23))
        ReportStatus.SINCRONIZADO ->
            if (dark) StatusColors(Color(0xFF2E6B55), Color(0xFFFBF7F1)) else StatusColors(Color(0xFF1B342D), Color(0xFFFBF7F1))
        ReportStatus.ERROR, ReportStatus.REQUIERE_CORRECCION ->
            if (dark) StatusColors(Color(0xFF7A2E12), Color(0xFFF6D5C8)) else StatusColors(Color(0xFFF6D5C8), Color(0xFF7A2E12))
    }
}

/**
 * El estado SIEMPRE se comunica con texto + ícono (no solo con color), como pide la guía visual.
 * Mientras se envía, el ícono es un indicador de carga animado.
 */
@Composable
fun StatusChip(status: ReportStatus, modifier: Modifier = Modifier) {
    val target = colorsFor(status)
    val container by animateColorAsState(target.container, label = "chip-fondo")
    val content by animateColorAsState(target.content, label = "chip-texto")

    Row(
        modifier = modifier
            .background(container, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (status) {
            ReportStatus.ENVIANDO -> CircularProgressIndicator(
                modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = content
            )
            ReportStatus.BORRADOR -> Icon(Icons.Default.Edit, null, Modifier.size(14.dp), tint = content)
            ReportStatus.EN_COLA -> Icon(painterResource(R.drawable.ic_clock), null, Modifier.size(14.dp), tint = content)
            ReportStatus.SINCRONIZADO -> Icon(Icons.Default.CheckCircle, null, Modifier.size(14.dp), tint = content)
            ReportStatus.ERROR, ReportStatus.REQUIERE_CORRECCION ->
                Icon(Icons.Default.Warning, null, Modifier.size(14.dp), tint = content)
        }
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = content
        )
    }
}
