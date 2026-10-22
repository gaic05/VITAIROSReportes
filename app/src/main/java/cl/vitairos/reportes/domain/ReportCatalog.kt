package cl.vitairos.reportes.domain

/** Catálogo cerrado del MVP (las listas son fijas, el usuario no escribe tipo ni proceso). */
object ReportCatalog {
    const val TIPO_TURNO = "Reporte de turno"
    const val TIPO_SEGURIDAD = "Inspección de seguridad"

    val tipos = listOf(TIPO_TURNO, "Mantención", TIPO_SEGURIDAD, "Control de calidad")
    val procesos = listOf("Extracción", "Chancado", "Transporte", "Mantención de equipos", "Seguridad")
    val areas = listOf("Faena Norte", "Faena Sur", "Planta")
    val turnos = listOf("A", "B", "C", "No aplica")

    const val MAX_ADJUNTOS = 5
    const val MAX_ARCHIVO_BYTES = 10L * 1024 * 1024
    const val OBS_MAX = 280
    const val OBS_MIN_SEGURIDAD = 10

    val mimePermitidos = setOf("image/jpeg", "image/png", "application/pdf")
}
