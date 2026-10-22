package cl.vitairos.reportes.ui.screens.report

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.vitairos.reportes.data.ReportRepository
import cl.vitairos.reportes.data.SyncManager
import cl.vitairos.reportes.data.files.AttachmentStore
import cl.vitairos.reportes.data.files.ImageQualityAnalyzer
import cl.vitairos.reportes.data.files.ImportResult
import cl.vitairos.reportes.data.local.toForm
import cl.vitairos.reportes.di.AppContainer
import cl.vitairos.reportes.domain.DraftAttachment
import cl.vitairos.reportes.domain.ReportCatalog
import cl.vitairos.reportes.domain.ReportForm
import cl.vitairos.reportes.domain.ReportFormErrors
import cl.vitairos.reportes.domain.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FormUiState(
    val form: ReportForm = ReportForm(),
    val errors: ReportFormErrors = ReportFormErrors(),
    val showErrors: Boolean = false,
    val attachError: String? = null,
    /** Alertas por adjunto (clave = ruta local): calidad de imagen y posible duplicado en otro reporte. */
    val warnings: Map<String, List<String>> = emptyMap(),
    val importing: Boolean = false,
    val legible: Boolean = false,
    val orientado: Boolean = false,
    val reviewError: String? = null,
    val saving: Boolean = false,
    val loading: Boolean = false
)

/**
 * ViewModel compartido por las 3 pantallas del flujo (Datos → Adjuntos → Revisión).
 * Vive mientras la Activity: así el borrador no se pierde al ir y volver entre pasos.
 */
class ReportFormViewModel(
    private val repository: ReportRepository,
    private val store: AttachmentStore,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _state = MutableStateFlow(FormUiState())
    val state: StateFlow<FormUiState> = _state.asStateFlow()

    /** Archivos copiados en esta sesión y todavía no guardados (se borran si se descarta el reporte). */
    private val archivosNuevos = mutableSetOf<String>()

    val hasContent: Boolean
        get() = _state.value.form.let {
            it.editId != null || it.tipo.isNotBlank() || it.proceso.isNotBlank() ||
                    it.observaciones.isNotBlank() || it.adjuntos.isNotEmpty()
        }

    // ---------- inicio / fin del flujo ----------

    fun startNew() {
        limpiarArchivosNuevos()
        _state.value = FormUiState()
    }

    fun startEdit(id: String) {
        limpiarArchivosNuevos()
        _state.value = FormUiState(loading = true)
        viewModelScope.launch {
            val details = repository.get(id)
            _state.value = if (details == null) {
                FormUiState(attachError = "El reporte ya no existe.")
            } else {
                FormUiState(form = details.toForm())
            }
        }
    }

    fun discard() {
        limpiarArchivosNuevos()
        _state.value = FormUiState()
    }

    // ---------- paso 1: datos ----------

    private fun updateForm(transform: (ReportForm) -> ReportForm) {
        _state.update { s ->
            val f = transform(s.form)
            s.copy(form = f, errors = if (s.showErrors) Validators.validateDatos(f) else s.errors)
        }
    }

    fun onTipo(value: String) = updateForm { it.copy(tipo = value) }
    fun onProceso(value: String) = updateForm { it.copy(proceso = value) }
    fun onArea(value: String) = updateForm { it.copy(area = value) }
    fun onTurno(value: String) = updateForm { it.copy(turno = value) }
    fun onObservaciones(value: String) = updateForm { it.copy(observaciones = value) }

    fun validateDatos(): Boolean {
        val errors = Validators.validateDatos(_state.value.form)
        _state.update { it.copy(errors = errors, showErrors = true) }
        return !errors.hasErrors
    }

    // ---------- paso 2: adjuntos ----------

    fun newCameraUri(): Uri = store.newCameraUri()

    fun addFromUri(uri: Uri) {
        if (_state.value.form.adjuntos.size >= ReportCatalog.MAX_ADJUNTOS) {
            _state.update { it.copy(attachError = "Máximo ${ReportCatalog.MAX_ADJUNTOS} archivos por reporte.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(importing = true, attachError = null) }
            when (val result = store.importUri(uri)) {
                is ImportResult.Rejected ->
                    _state.update { it.copy(importing = false, attachError = result.reason) }
                is ImportResult.Success -> {
                    val nuevo = result.attachment
                    if (_state.value.form.adjuntos.any { it.sha256 == nuevo.sha256 }) {
                        store.delete(listOf(nuevo.rutaLocal)) // mismo archivo ya adjunto (hash SHA-256 igual)
                        _state.update { it.copy(importing = false, attachError = "Ese archivo ya está adjunto en este reporte.") }
                    } else {
                        archivosNuevos.add(nuevo.rutaLocal)
                        val avisos = analizar(nuevo)
                        _state.update {
                            it.copy(
                                importing = false,
                                form = it.form.copy(adjuntos = it.form.adjuntos + nuevo),
                                warnings = if (avisos.isEmpty()) it.warnings else it.warnings + (nuevo.rutaLocal to avisos)
                            )
                        }
                    }
                }
            }
        }
    }

    fun removeAttachment(index: Int) {
        val quitado = _state.value.form.adjuntos.getOrNull(index) ?: return
        if (archivosNuevos.remove(quitado.rutaLocal)) {
            viewModelScope.launch(Dispatchers.IO) { store.delete(listOf(quitado.rutaLocal)) }
        }
        _state.update {
            it.copy(
                form = it.form.copy(adjuntos = it.form.adjuntos.filterIndexed { i, _ -> i != index }),
                warnings = it.warnings - quitado.rutaLocal,
                attachError = null
            )
        }
    }

    /** Alertas del archivo recién adjuntado: calidad de imagen y si ya está en otro reporte. */
    private suspend fun analizar(adjunto: DraftAttachment): List<String> = withContext(Dispatchers.IO) {
        val avisos = mutableListOf<String>()
        if (adjunto.esImagen) {
            ImageQualityAnalyzer.analyze(adjunto.rutaLocal).forEach { avisos += it.mensaje }
        }
        val otro = repository.findReportWithSameFile(adjunto.sha256, _state.value.form.editId)
        if (otro != null) {
            avisos += "Este archivo ya está en el reporte $otro. Verifica que no sea un envío duplicado."
        }
        avisos
    }

    fun validateAdjuntos(): Boolean {
        val error = Validators.validateAdjuntos(_state.value.form)
        _state.update { it.copy(attachError = error) }
        return error == null
    }

    // ---------- paso 3: revisión y guardado ----------

    fun toggleLegible() = _state.update { it.copy(legible = !it.legible, reviewError = null) }
    fun toggleOrientado() = _state.update { it.copy(orientado = !it.orientado, reviewError = null) }

    /** enqueue = true → "En cola" (se envía); false → "Borrador". */
    fun save(enqueue: Boolean, usuario: String, onSaved: () -> Unit) {
        val s = _state.value
        if (s.saving) return
        if (enqueue) {
            val error = Validators.validateRevision(s.legible, s.orientado)
            if (error != null) {
                _state.update { it.copy(reviewError = error) }
                return
            }
        }
        _state.update { it.copy(saving = true, reviewError = null) }
        viewModelScope.launch {
            try {
                val id = repository.saveReport(s.form, usuario, enqueue)
                archivosNuevos.clear()
                if (enqueue) {
                    if (syncManager.isOnline) {
                        syncManager.post("$id guardado en cola. Enviando…")
                        syncManager.syncReport(id)
                    } else {
                        syncManager.post("$id guardado sin conexión. Se enviará al recuperar la red.")
                    }
                } else {
                    syncManager.post("$id guardado como borrador.")
                }
                _state.value = FormUiState()
                onSaved()
            } catch (e: Exception) {
                _state.update { it.copy(saving = false, reviewError = e.message ?: "No se pudo guardar el reporte.") }
            }
        }
    }

    private fun limpiarArchivosNuevos() {
        val rutas = archivosNuevos.toList()
        archivosNuevos.clear()
        if (rutas.isNotEmpty()) viewModelScope.launch(Dispatchers.IO) { store.delete(rutas) }
    }

    companion object {
        fun factory(c: AppContainer) = viewModelFactory {
            initializer { ReportFormViewModel(c.repository, c.attachmentStore, c.syncManager) }
        }
    }
}