package cl.vitairos.reportes.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.vitairos.reportes.data.ReportRepository
import cl.vitairos.reportes.data.SyncManager
import cl.vitairos.reportes.data.local.ReportWithDetails
import cl.vitairos.reportes.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val details: ReportWithDetails? = null,
    val confirmDelete: Boolean = false,
    val deleted: Boolean = false
)

class DetailViewModel(
    private val id: String,
    private val repository: ReportRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val showConfirm = MutableStateFlow(false)
    private val wasDeleted = MutableStateFlow(false)

    val uiState: StateFlow<DetailUiState> =
        combine(repository.observe(id), showConfirm, wasDeleted) { details, confirm, deleted ->
            DetailUiState(details, confirm, deleted)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    fun askDelete() {
        showConfirm.value = true
    }

    fun cancelDelete() {
        showConfirm.value = false
    }

    fun confirmDelete() {
        showConfirm.value = false
        viewModelScope.launch {
            if (repository.deleteReport(id)) {
                syncManager.post("$id eliminado.")
                wasDeleted.value = true
            } else {
                syncManager.post("$id ya no puede eliminarse en su estado actual.")
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            if (repository.requeueForRetry(id)) syncManager.syncReport(id)
        }
    }

    fun markObserved() {
        viewModelScope.launch {
            if (repository.markObserved(id)) syncManager.post("$id marcado como Requiere corrección.")
        }
    }

    companion object {
        fun factory(c: AppContainer, id: String) = viewModelFactory {
            initializer { DetailViewModel(id, c.repository, c.syncManager) }
        }
    }
}
