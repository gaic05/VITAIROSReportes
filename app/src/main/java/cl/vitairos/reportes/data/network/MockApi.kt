package cl.vitairos.reportes.data.network

import cl.vitairos.reportes.data.local.ReportWithDetails
import kotlinx.coroutines.delay

data class ApiResponse(
    val ok: Boolean,
    val receivedId: String?,
    val status: String,
    val message: String
)

/**
 * SIMULACIÓN de la API de VITAIROS Industrial AI (acordado en el cuestionario: basta una API mock).
 * Devuelve confirmación de recepción, identificador recibido, estado y mensaje.
 * - Si el interruptor "Simular falla" está activo, responde con error.
 * - Si no hay conexión, responde con error de red.
 */
class MockApi(
    private val isOnline: () -> Boolean,
    private val shouldFail: () -> Boolean
) {
    suspend fun enviarReporte(details: ReportWithDetails): ApiResponse {
        delay(LATENCIA_MS)
        return when {
            !isOnline() -> ApiResponse(false, null, "SIN_RED", "Sin conexión durante el envío.")
            shouldFail() -> ApiResponse(false, null, "ERROR", "Error de red simulado (API mock).")
            else -> ApiResponse(
                ok = true,
                receivedId = details.report.id,
                status = "RECIBIDO",
                message = "recepción confirmada por API mock (simulación)"
            )
        }
    }

    private companion object {
        const val LATENCIA_MS = 1500L
    }
}
