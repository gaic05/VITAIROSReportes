package cl.vitairos.reportes.domain

import java.util.UUID

/** Identificador único generado al crear el reporte (funciona sin conexión). Ej: VTR-3F9A12BC */
object ReportIdGenerator {
    private val FORMAT = Regex("^VTR-[0-9A-F]{8}$")

    fun newId(): String = "VTR-" + UUID.randomUUID().toString().replace("-", "").take(8).uppercase()

    fun isValid(id: String) = FORMAT.matches(id)
}
