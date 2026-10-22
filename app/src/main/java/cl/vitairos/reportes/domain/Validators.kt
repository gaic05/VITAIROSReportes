package cl.vitairos.reportes.domain

import java.util.Locale

/** Validaciones puras (sin Android) para poder probarlas con tests unitarios. */
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(value: String): String? = when {
        value.isBlank() -> "Ingresa tu correo"
        !EMAIL_REGEX.matches(value.trim()) -> "Correo no válido"
        else -> null
    }

    fun validatePassword(value: String): String? = when {
        value.isBlank() -> "Ingresa tu contraseña"
        value.length < 6 -> "Mínimo 6 caracteres"
        else -> null
    }

    /** Paso 1 del flujo: datos del reporte. Incluye reglas condicionales por tipo. */
    fun validateDatos(form: ReportForm): ReportFormErrors {
        val obs = form.observaciones.trim()
        return ReportFormErrors(
            tipo = if (form.tipo.isBlank()) "Selecciona un tipo de reporte" else null,
            proceso = if (form.proceso.isBlank()) "Selecciona un proceso asociado" else null,
            area = if (form.area.isBlank()) "Selecciona el área / faena" else null,
            turno = if (form.tipo == ReportCatalog.TIPO_TURNO && form.turno.isBlank())
                "El turno es obligatorio en reportes de turno" else null,
            observaciones = when {
                form.observaciones.length > ReportCatalog.OBS_MAX ->
                    "Máximo ${ReportCatalog.OBS_MAX} caracteres"
                form.tipo == ReportCatalog.TIPO_SEGURIDAD && obs.length < ReportCatalog.OBS_MIN_SEGURIDAD ->
                    "Describe el hallazgo (mínimo ${ReportCatalog.OBS_MIN_SEGURIDAD} caracteres)"
                else -> null
            }
        )
    }

    /** Paso 2 del flujo: adjuntos. */
    fun validateAdjuntos(form: ReportForm): String? = when {
        form.adjuntos.isEmpty() -> "Agrega al menos un adjunto."
        form.adjuntos.size > ReportCatalog.MAX_ADJUNTOS -> "Máximo ${ReportCatalog.MAX_ADJUNTOS} archivos por reporte."
        else -> null
    }

    /** Validación objetiva de un archivo antes de adjuntarlo (formato y tamaño). tamano = -1 si se desconoce. */
    fun validateArchivo(mime: String?, tamanoBytes: Long): String? = when {
        mime == null || mime !in ReportCatalog.mimePermitidos ->
            "Formato no permitido. Usa JPG, PNG o PDF."
        tamanoBytes == 0L -> "El archivo está vacío."
        tamanoBytes > ReportCatalog.MAX_ARCHIVO_BYTES -> {
            val mb = tamanoBytes / (1024.0 * 1024.0)
            "El archivo pesa ${String.format(Locale.US, "%.1f", mb)} MB y supera el límite de 10 MB. No se adjuntó."
        }
        else -> null
    }

    /** Paso 3: confirmación de la revisión básica de calidad. */
    fun validateRevision(legible: Boolean, bienOrientado: Boolean): String? =
        if (legible && bienOrientado) null
        else "Confirma que el documento es legible y está bien orientado."
}
