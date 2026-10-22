package cl.vitairos.reportes.domain

/** Estados mínimos pedidos por VITAIROS + "Requiere corrección" (extensión permitida). */
enum class ReportStatus(val label: String) {
    BORRADOR("Borrador"),
    EN_COLA("En cola"),
    ENVIANDO("Enviando"),
    SINCRONIZADO("Sincronizado"),
    ERROR("Error"),
    REQUIERE_CORRECCION("Requiere corrección")
}

enum class UserRole(val label: String) {
    OPERADOR("Operador"),
    SUPERVISOR("Supervisor")
}

enum class ThemeMode { SISTEMA, CLARO, OSCURO }

/** Sesión simulada. Nunca guarda la contraseña. */
data class Session(val email: String, val nombre: String, val role: UserRole)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SISTEMA,
    val session: Session? = null,
    val simulateOffline: Boolean = false,
    val simulateApiFailure: Boolean = false
)

/** Archivo ya copiado al almacenamiento privado de la app. */
data class DraftAttachment(
    val nombre: String,
    val mime: String,
    val tamanoBytes: Long,
    val rutaLocal: String,
    val sha256: String
) {
    val esImagen: Boolean get() = mime.startsWith("image/")
}

/** Datos del formulario "Nuevo reporte" mientras se completa (aún no está en la base de datos). */
data class ReportForm(
    val tipo: String = "",
    val proceso: String = "",
    val area: String = "",
    val turno: String = "",
    val observaciones: String = "",
    val adjuntos: List<DraftAttachment> = emptyList(),
    val editId: String? = null
)

data class ReportFormErrors(
    val tipo: String? = null,
    val proceso: String? = null,
    val area: String? = null,
    val turno: String? = null,
    val observaciones: String? = null
) {
    val hasErrors: Boolean
        get() = listOf(tipo, proceso, area, turno, observaciones).any { it != null }
}
