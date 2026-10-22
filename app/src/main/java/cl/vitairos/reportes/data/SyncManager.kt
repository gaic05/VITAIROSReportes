package cl.vitairos.reportes.data

import cl.vitairos.reportes.domain.SyncDecision
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Orquesta la cola de envíos: sincroniza automáticamente al recuperar la conexión, aplica la
 * política de reintentos (5, 15 y 30 s) y avisa al usuario con mensajes dentro de la app.
 */
class SyncManager(
    private val repository: ReportRepository,
    private val online: StateFlow<Boolean>,
    private val scope: CoroutineScope
) {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    /** Reportes que ya tienen un envío en curso (evita enviar dos veces el mismo). */
    private val enCurso = ConcurrentHashMap.newKeySet<String>()

    val isOnline: Boolean get() = online.value

    fun post(message: String) {
        _messages.tryEmit(message)
    }

    fun start() {
        scope.launch {
            repository.recoverInterrupted()
            if (online.value) syncQueue(silentIfEmpty = true)
        }
        // Intento automático al recuperar la conexión.
        scope.launch {
            online.drop(1).collect { conectado ->
                if (conectado) {
                    val n = repository.queuedIds().size
                    if (n > 0) {
                        post("Conexión recuperada. Sincronizando $n reporte(s)…")
                        syncQueue(silentIfEmpty = true)
                    }
                } else {
                    post("Sin conexión. Puedes seguir creando reportes.")
                }
            }
        }
    }

    /** Botón "Sincronizar ahora" (manual). */
    fun syncQueue(silentIfEmpty: Boolean = false) {
        scope.launch {
            val ids = repository.queuedIds()
            if (ids.isEmpty()) {
                if (!silentIfEmpty) post("No hay reportes en cola.")
            } else {
                ids.forEach { syncReport(it) }
            }
        }
    }

    fun syncReport(id: String) {
        if (!enCurso.add(id)) return
        scope.launch {
            try {
                runAttempts(id)
            } finally {
                enCurso.remove(id)
            }
        }
    }

    private suspend fun runAttempts(id: String) {
        while (true) {
            if (!online.value) {
                post("Sin conexión. $id permanece en cola y se enviará al recuperar la red.")
                return
            }
            when (val result = repository.attemptSync(id)) {
                SyncOutcome.Skipped -> return
                SyncOutcome.Success -> {
                    post("$id enviado correctamente.")
                    return
                }
                is SyncOutcome.Failed -> when (val decision = result.decision) {
                    is SyncDecision.RetryIn -> delay(decision.seconds * 1000L)
                    SyncDecision.GiveUp -> {
                        post("Error al enviar $id. Usa Reintentar.")
                        return
                    }
                }
            }
        }
    }
}
