package cl.vitairos.reportes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.vitairos.reportes.data.SyncManager
import cl.vitairos.reportes.data.local.SettingsRepository
import cl.vitairos.reportes.di.AppContainer
import cl.vitairos.reportes.domain.AppSettings
import cl.vitairos.reportes.domain.ThemeMode
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Estado global de la app: tema, sesión, conectividad, interruptores de simulación y mensajes. */
class AppViewModel(
    private val settingsRepository: SettingsRepository,
    val settings: StateFlow<AppSettings?>,
    val isOnline: StateFlow<Boolean>,
    syncManager: SyncManager
) : ViewModel() {

    val messages: SharedFlow<String> = syncManager.messages

    fun toggleTheme(currentlyDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(if (currentlyDark) ThemeMode.CLARO else ThemeMode.OSCURO)
        }
    }

    fun logout() {
        viewModelScope.launch { settingsRepository.setSession(null) }
    }

    fun setSimulateOffline(value: Boolean) {
        viewModelScope.launch { settingsRepository.setSimulateOffline(value) }
    }

    fun setSimulateApiFailure(value: Boolean) {
        viewModelScope.launch { settingsRepository.setSimulateApiFailure(value) }
    }

    companion object {
        fun factory(c: AppContainer) = viewModelFactory {
            initializer { AppViewModel(c.settingsRepository, c.settingsState, c.isOnline, c.syncManager) }
        }
    }
}
