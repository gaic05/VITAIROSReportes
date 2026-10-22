package cl.vitairos.reportes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.vitairos.reportes.data.ReportRepository
import cl.vitairos.reportes.data.SyncManager
import cl.vitairos.reportes.data.local.ReportWithDetails
import cl.vitairos.reportes.di.AppContainer
import cl.vitairos.reportes.domain.ReportMetrics
import cl.vitairos.reportes.domain.ReportRules
import cl.vitairos.reportes.domain.ReportSample
import cl.vitairos.reportes.domain.ReportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilter(val label: String) {
    TODOS("Todos"),
    PENDIENTES("Pendientes"),
    SINCRONIZADOS("Sincronizados"),
    CON_ERROR("Con error")
}

data class ReportsUiState(
    val reports: List<ReportWithDetails> = emptyList(),
    val filter: HistoryFilter = HistoryFilter.TODOS,
    val loading: Boolean = true
) {
    /** Pantalla "Cola de envíos": En cola, Enviando, Error y Requiere corrección. */
    val queue: List<ReportWithDetails> get() = reports.filter { ReportRules.isInQueue(it.report.estado) }

    val recent: List<ReportWithDetails> get() = reports.take(3)

    val filtered: List<ReportWithDetails>
        get() = reports.filter {
            when (filter) {
                HistoryFilter.TODOS -> true
                HistoryFilter.PENDIENTES -> it.report.estado in PENDIENTES
                HistoryFilter.SINCRONIZADOS -> it.report.estado == ReportStatus.SINCRONIZADO
                HistoryFilter.CON_ERROR ->
                    it.report.estado == ReportStatus.ERROR || it.report.estado == ReportStatus.REQUIERE_CORRECCION
            }
        }

    val countBorradores: Int get() = reports.count { it.report.estado == ReportStatus.BORRADOR }
    val countEnCola: Int
        get() = reports.count { it.report.estado == ReportStatus.EN_COLA || it.report.estado == ReportStatus.ENVIANDO }
    val countConError: Int
        get() = reports.count { it.report.estado == ReportStatus.ERROR || it.report.estado == ReportStatus.REQUIERE_CORRECCION }
    val countSincronizados: Int get() = reports.count { it.report.estado == ReportStatus.SINCRONIZADO }

    /** Indicadores de éxito del caso (tiempo a envío, metadata completa, envíos exitosos). */
    val metrics: ReportMetrics
        get() = ReportMetrics.from(
            reports.map {
                ReportSample(
                    tipo = it.report.tipo, proceso = it.report.proceso, area = it.report.area,
                    turno = it.report.turno, estado = it.report.estado, intentos = it.report.intentos,
                    creadoEn = it.report.creadoEn, sincronizadoEn = it.report.sincronizadoEn,
                    adjuntos = it.attachments.size
                )
            }
        )

    private companion object {
        val PENDIENTES = setOf(ReportStatus.BORRADOR, ReportStatus.EN_COLA, ReportStatus.ENVIANDO)
    }
}

/** Alimenta Inicio, Cola e Historial: la UI solo observa este estado y la pantalla se actualiza sola. */
class ReportsViewModel(
    private val repository: ReportRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val filter = MutableStateFlow(HistoryFilter.TODOS)

    val uiState: StateFlow<ReportsUiState> =
        combine(repository.observeAll(), filter) { reports, f ->
            ReportsUiState(reports = reports, filter = f, loading = false)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

    fun setFilter(value: HistoryFilter) {
        filter.value = value
    }

    fun syncNow() = syncManager.syncQueue()

    fun retry(id: String) {
        viewModelScope.launch {
            if (repository.requeueForRetry(id)) syncManager.syncReport(id)
        }
    }

    companion object {
        fun factory(c: AppContainer) = viewModelFactory {
            initializer { ReportsViewModel(c.repository, c.syncManager) }
        }
    }
}
