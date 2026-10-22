package cl.vitairos.reportes.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cl.vitairos.reportes.domain.ReportMetrics
import cl.vitairos.reportes.domain.Session
import cl.vitairos.reportes.domain.UserRole
import cl.vitairos.reportes.ui.components.BigOutlinedButton
import cl.vitairos.reportes.ui.components.KeyValueRow
import cl.vitairos.reportes.ui.components.ScreenScaffold
import cl.vitairos.reportes.ui.components.SectionLabel
import cl.vitairos.reportes.ui.components.SurfaceCard
import cl.vitairos.reportes.ui.screens.ReportsUiState
import cl.vitairos.reportes.ui.theme.Hueso
import cl.vitairos.reportes.ui.theme.Oro
import cl.vitairos.reportes.ui.theme.VerdeMineral

/** Perfil del trabajador que inició sesión (datos de la sesión simulada + resumen de su actividad). */
@Composable
fun ProfileScreen(
    session: Session,
    state: ReportsUiState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val iniciales = session.nombre
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

    val funcion = when (session.role) {
        UserRole.OPERADOR -> "Registra reportes de terreno, adjunta documentos y los envía a VITAIROS."
        UserRole.SUPERVISOR -> "Consulta y revisa los reportes registrados desde terreno."
    }

    ScreenScaffold(title = "Mi perfil", onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera: avatar con iniciales, nombre y rol.
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(VerdeMineral)
                        .border(2.dp, Oro, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iniciales, style = MaterialTheme.typography.headlineMedium, color = Hueso)
                }
                Text(session.nombre, style = MaterialTheme.typography.headlineSmall)
                Text(
                    session.role.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            SectionLabel("Datos del trabajador")
            SurfaceCard {
                KeyValueRow("Nombre", session.nombre)
                KeyValueRow("Correo", session.email)
                KeyValueRow("Perfil", session.role.label)
                KeyValueRow("Empresa", "VITAIROS Industrial AI")
                KeyValueRow("Sesión", "Activa (simulada)")
            }

            SectionLabel("Mi función")
            Text(funcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            SectionLabel("Mi actividad")
            SurfaceCard {
                KeyValueRow("Reportes", state.reports.size.toString())
                KeyValueRow("En cola", state.countEnCola.toString())
                KeyValueRow("Sincronizados", state.countSincronizados.toString())
            }

            val m = state.metrics
            SectionLabel("Indicadores de envío")
            SurfaceCard {
                KeyValueRow("Tiempo a envío", m.tiempoPromedioMs?.let { ReportMetrics.formatDuracion(it) } ?: "Sin envíos aún")
                KeyValueRow("Metadata completa", m.pctMetadata?.let { "$it % (${m.metadataCompleta} de ${m.registrados})" } ?: "Sin reportes aún")
                KeyValueRow("Envíos exitosos", m.pctExito?.let { "$it % (${m.enviosExitosos} de ${m.enviosIntentados})" } ?: "Sin envíos aún")
                KeyValueRow("Con reintentos", m.recuperadosConReintentos.toString())
            }
            Text(
                "Tiempo a envío: promedio entre la captura y la confirmación de la API (simulada). " +
                        "Con reintentos: reportes que se enviaron tras una falla de conexión. Se calculan con los reportes de este dispositivo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            BigOutlinedButton(text = "Ir a ajustes", onClick = onOpenSettings, modifier = Modifier.padding(top = 4.dp))
            BigOutlinedButton(text = "Cerrar sesión", onClick = onLogout)
        }
    }
}

