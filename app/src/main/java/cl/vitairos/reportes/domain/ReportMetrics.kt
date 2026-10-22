package cl.vitairos.reportes.domain

import cl.vitairos.reportes.domain.ReportMetrics
import cl.vitairos.reportes.domain.ReportSample
data class ReportSample(
    val tipo: String,
    val proceso: String,
    val area: String,
    val turno: String,
    val estado: ReportStatus,
    val intentos: Int,
    val creadoEn: Long,
    val sincronizadoEn: Long?,
    val adjuntos: Int
)

data class ReportMetrics(
    val registrados: Int,
    val metadataCompleta: Int,
    val enviosIntentados: Int,
    val enviosExitosos: Int,
    val recuperadosConReintentos: Int,
    val tiempoPromedioMs: Long?
) {
    val pctMetadata: Int? get() = pct(metadataCompleta, registrados)
    val pctExito: Int? get() = pct(enviosExitosos, enviosIntentados)

    companion object {
        fun from(samples: List<ReportSample>): ReportMetrics {
            // Los borradores aún no se entregan: no cuentan para la metadata ni para los envíos.
            val registrados = samples.filter { it.estado != ReportStatus.BORRADOR }
            val sincronizados = samples.filter { it.sincronizadoEn != null }
            val intentados = samples.filter { it.sincronizadoEn != null || it.estado == ReportStatus.ERROR }
            val tiempos = sincronizados.map { (it.sincronizadoEn!! - it.creadoEn).coerceAtLeast(0L) }
            return ReportMetrics(
                registrados = registrados.size,
                metadataCompleta = registrados.count { it.metadataCompleta() },
                enviosIntentados = intentados.size,
                enviosExitosos = sincronizados.size,
                recuperadosConReintentos = sincronizados.count { it.intentos > 1 },
                tiempoPromedioMs = if (tiempos.isEmpty()) null else tiempos.sum() / tiempos.size
            )
        }

        /** Metadata mínima: tipo, proceso, área, turno (obligatorio en reportes de turno) y al menos un adjunto. */
        private fun ReportSample.metadataCompleta(): Boolean =
            tipo.isNotBlank() && proceso.isNotBlank() && area.isNotBlank() && adjuntos > 0 &&
                    (tipo != ReportCatalog.TIPO_TURNO || turno.isNotBlank())

        private fun pct(part: Int, total: Int): Int? =
            if (total == 0) null else Math.round(part * 100f / total)

        /** 850 ms → "< 1 s" · 45 s → "45 s" · 134 s → "2 min 14 s" · 2 h 5 min → "2 h 5 min". */
        fun formatDuracion(ms: Long): String {
            val s = ms / 1000
            return when {
                s < 1 -> "< 1 s"
                s < 60 -> "$s s"
                s < 3600 -> "${s / 60} min ${s % 60} s"
                else -> "${s / 3600} h ${(s % 3600) / 60} min"
            }
        }
    }
}

