package cl.vitairos.reportes.di

import android.content.Context
import androidx.room.Room
import cl.vitairos.reportes.data.ReportRepository
import cl.vitairos.reportes.data.SyncManager
import cl.vitairos.reportes.data.files.AttachmentStore
import cl.vitairos.reportes.data.local.AppDatabase
import cl.vitairos.reportes.data.local.SettingsRepository
import cl.vitairos.reportes.data.local.settingsDataStore
import cl.vitairos.reportes.data.network.ConnectivityMonitor
import cl.vitairos.reportes.data.network.MockApi
import cl.vitairos.reportes.domain.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Inyección de dependencias manual (sin Hilt): aquí se arma y se comparte cada pieza de la app. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settingsRepository = SettingsRepository(appContext.settingsDataStore)

    /** null mientras DataStore carga por primera vez. */
    val settingsState: StateFlow<AppSettings?> =
        settingsRepository.settings.stateIn(appScope, SharingStarted.Eagerly, null)

    private val connectivity = ConnectivityMonitor(appContext)

    /** Red real combinada con el interruptor "Simular sin conexión" (para demostrar el modo offline). */
    val isOnline: StateFlow<Boolean> = combine(
        connectivity.hasNetwork,
        settingsRepository.settings.map { it.simulateOffline }.distinctUntilChanged()
    ) { real, simulatedOffline -> real && !simulatedOffline }
        .distinctUntilChanged()
        .stateIn(appScope, SharingStarted.Eagerly, true)

    private val database: AppDatabase =
        Room.databaseBuilder(appContext, AppDatabase::class.java, "vitairos.db").build()

    val attachmentStore = AttachmentStore(appContext)

    private val mockApi = MockApi(
        isOnline = { isOnline.value },
        shouldFail = { settingsState.value?.simulateApiFailure == true }
    )

    val repository = ReportRepository(database, attachmentStore, mockApi)

    val syncManager = SyncManager(repository, isOnline, appScope)

    fun start() = syncManager.start()
}
