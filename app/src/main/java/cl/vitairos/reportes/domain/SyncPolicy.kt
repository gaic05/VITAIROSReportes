package cl.vitairos.reportes.domain

sealed interface SyncDecision {
    data class RetryIn(val seconds: Int) : SyncDecision
    data object GiveUp : SyncDecision
}

/**
 * Política de reintentos acordada con VITAIROS:
 * 3 reintentos automáticos con espera de 5, 15 y 30 s; luego estado Error + reintento manual.
 * Total: 1 intento inicial + 3 reintentos = 4 intentos.
 */
object SyncPolicy {
    const val MAX_AUTO_RETRIES = 3
    val BACKOFF_SECONDS = listOf(5, 15, 30)

    /** [attemptsDone] = intentos realizados hasta ahora, incluyendo el que acaba de fallar. */
    fun afterFailure(attemptsDone: Int): SyncDecision =
        if (attemptsDone <= MAX_AUTO_RETRIES) {
            SyncDecision.RetryIn(BACKOFF_SECONDS[(attemptsDone - 1).coerceIn(0, BACKOFF_SECONDS.lastIndex)])
        } else {
            SyncDecision.GiveUp
        }
}
