package cl.vitairos.reportes.ui.screens.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.vitairos.reportes.domain.Session
import cl.vitairos.reportes.ui.components.AttachmentThumb
import cl.vitairos.reportes.ui.components.AttachmentWarnings
import cl.vitairos.reportes.ui.components.BigButton
import cl.vitairos.reportes.ui.components.BigOutlinedButton
import cl.vitairos.reportes.ui.components.InlineMessage
import cl.vitairos.reportes.ui.components.KeyValueRow
import cl.vitairos.reportes.ui.components.ScreenScaffold
import cl.vitairos.reportes.ui.components.SectionLabel
import cl.vitairos.reportes.ui.components.SurfaceCard
import cl.vitairos.reportes.ui.formatSize
import coil.compose.AsyncImage
import java.io.File

/** Paso 3 de 3: revisión básica de calidad (vista previa + confirmación) y guardado. */
@Composable
fun ReviewScreen(
    viewModel: ReportFormViewModel,
    session: Session,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val form = state.form

    ScreenScaffold(title = "Confirmar reporte", onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionLabel("Paso 3 de 3 · Revisión")

            SurfaceCard {
                KeyValueRow("Tipo", form.tipo)
                KeyValueRow("Proceso", form.proceso)
                KeyValueRow("Área / faena", form.area)
                KeyValueRow("Turno", form.turno.ifBlank { "—" })
                KeyValueRow("Observaciones", form.observaciones.ifBlank { "—" })
                KeyValueRow("Adjuntos", form.adjuntos.size.toString())
                KeyValueRow("Usuario", session.nombre)
            }

            SectionLabel("Vista previa del documento")
            form.adjuntos.forEach { a ->
                SurfaceCard {
                    if (a.esImagen) {
                        AsyncImage(
                            model = File(a.rutaLocal),
                            contentDescription = "Vista previa de ${a.nombre}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AttachmentThumb(path = a.rutaLocal, mime = a.mime, size = 56.dp)
                            Text("Documento PDF (sin vista previa en este prototipo)", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Text(
                        "${a.nombre} · ${formatSize(a.tamanoBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AttachmentWarnings(state.warnings[a.rutaLocal])
                }
            }

            CheckRow("El documento es legible", state.legible, viewModel::toggleLegible)
            CheckRow("El documento está bien orientado y es el esperado", state.orientado, viewModel::toggleOrientado)

            InlineMessage(state.reviewError)

            BigButton(
                text = "Guardar y poner en cola",
                loading = state.saving,
                onClick = { viewModel.save(enqueue = true, usuario = session.nombre, onSaved = onSaved) }
            )
            BigOutlinedButton(
                text = "Guardar como borrador",
                enabled = !state.saving,
                onClick = { viewModel.save(enqueue = false, usuario = session.nombre, onSaved = onSaved) }
            )
        }
    }
}

@Composable
private fun CheckRow(text: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
