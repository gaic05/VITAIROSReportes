package cl.vitairos.reportes.ui.screens.report

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.vitairos.reportes.domain.ReportCatalog
import cl.vitairos.reportes.domain.Session
import cl.vitairos.reportes.ui.components.BigButton
import cl.vitairos.reportes.ui.components.ConfirmDialog
import cl.vitairos.reportes.ui.components.FormDropdown
import cl.vitairos.reportes.ui.components.ScreenScaffold
import cl.vitairos.reportes.ui.components.SectionLabel
import cl.vitairos.reportes.ui.components.SurfaceCard
import cl.vitairos.reportes.ui.components.ValidatedTextField

/** Paso 1 de 3: tipo, proceso, área/faena, turno y observaciones (con validación por campo). */
@Composable
fun NewReportScreen(
    viewModel: ReportFormViewModel,
    session: Session,
    onNext: () -> Unit,
    onExit: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val form = state.form
    var confirmExit by remember { mutableStateOf(false) }

    val requestExit = {
        if (viewModel.hasContent) {
            confirmExit = true
        } else {
            viewModel.discard()
            onExit()
        }
    }
    BackHandler(onBack = requestExit)

    val turnoObligatorio = form.tipo == ReportCatalog.TIPO_TURNO
    val obsObligatoria = form.tipo == ReportCatalog.TIPO_SEGURIDAD

    ScreenScaffold(
        title = if (form.editId != null) "Editar reporte" else "Nuevo reporte",
        onBack = requestExit
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionLabel("Paso 1 de 3 · Datos del reporte")
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())

            SurfaceCard {
                Text(
                    "Se registran automáticamente: identificador, fecha y hora, usuario (${session.nombre}) y estado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FormDropdown(
                label = "Tipo de reporte *",
                value = form.tipo,
                options = ReportCatalog.tipos,
                onSelect = viewModel::onTipo,
                error = state.errors.tipo
            )
            FormDropdown(
                label = "Proceso asociado *",
                value = form.proceso,
                options = ReportCatalog.procesos,
                onSelect = viewModel::onProceso,
                error = state.errors.proceso
            )
            FormDropdown(
                label = "Área / faena *",
                value = form.area,
                options = ReportCatalog.areas,
                onSelect = viewModel::onArea,
                error = state.errors.area
            )
            FormDropdown(
                label = if (turnoObligatorio) "Turno *" else "Turno (opcional)",
                value = form.turno,
                options = ReportCatalog.turnos,
                onSelect = viewModel::onTurno,
                error = state.errors.turno
            )
            ValidatedTextField(
                value = form.observaciones,
                onValueChange = viewModel::onObservaciones,
                label = if (obsObligatoria) "Observaciones *" else "Observaciones (opcional)",
                error = state.errors.observaciones,
                singleLine = false,
                minLines = 3,
                hint = "${form.observaciones.length}/${ReportCatalog.OBS_MAX}",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
            )

            BigButton(
                text = "Continuar a adjuntos",
                onClick = { if (viewModel.validateDatos()) onNext() }
            )
        }
    }

    if (confirmExit) {
        ConfirmDialog(
            title = "¿Descartar el reporte?",
            text = "Se perderán los datos y adjuntos que aún no guardaste.",
            confirmLabel = "Descartar",
            dismissLabel = "Seguir editando",
            onConfirm = {
                confirmExit = false
                viewModel.discard()
                onExit()
            },
            onDismiss = { confirmExit = false }
        )
    }
}
