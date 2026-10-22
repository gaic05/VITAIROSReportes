package cl.vitairos.reportes.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import cl.vitairos.reportes.domain.DraftAttachment
import cl.vitairos.reportes.domain.ReportForm
import cl.vitairos.reportes.domain.ReportStatus

/** Tabla principal: un reporte operacional con su metadata y estado de sincronización. */
@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val tipo: String,
    val proceso: String,
    val area: String,
    val turno: String,
    val observaciones: String,
    val usuario: String,
    val creadoEn: Long,
    val actualizadoEn: Long,
    val estado: ReportStatus,
    val intentos: Int,
    val version: Int,
    val mensaje: String,
    val sincronizadoEn: Long?
)

/** Un reporte tiene de 1 a 5 adjuntos. rutaLocal pasa a null cuando el archivo se borra tras sincronizar. */
@Entity(
    tableName = "attachments",
    foreignKeys = [ForeignKey(
        entity = ReportEntity::class,
        parentColumns = ["id"],
        childColumns = ["reportId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("reportId")]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: String,
    val nombre: String,
    val tipoMime: String,
    val tamanoBytes: Long,
    val rutaLocal: String?,
    val sha256: String
)

/** Trazabilidad: cada cambio relevante del reporte queda registrado con su hora. */
@Entity(
    tableName = "report_log",
    foreignKeys = [ForeignKey(
        entity = ReportEntity::class,
        parentColumns = ["id"],
        childColumns = ["reportId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("reportId")]
)
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: String,
    val timestamp: Long,
    val texto: String
)

/** Reporte + adjuntos + trazabilidad (relación 1-N resuelta por Room). */
data class ReportWithDetails(
    @Embedded val report: ReportEntity,
    @Relation(parentColumn = "id", entityColumn = "reportId")
    val attachments: List<AttachmentEntity>,
    @Relation(parentColumn = "id", entityColumn = "reportId")
    val log: List<LogEntity>
)

fun DraftAttachment.toEntity(reportId: String) = AttachmentEntity(
    reportId = reportId,
    nombre = nombre,
    tipoMime = mime,
    tamanoBytes = tamanoBytes,
    rutaLocal = rutaLocal,
    sha256 = sha256
)

/** Convierte un reporte guardado en el formulario para editarlo (solo adjuntos con archivo local). */
fun ReportWithDetails.toForm() = ReportForm(
    tipo = report.tipo,
    proceso = report.proceso,
    area = report.area,
    turno = report.turno,
    observaciones = report.observaciones,
    adjuntos = attachments.mapNotNull { a ->
        a.rutaLocal?.let { DraftAttachment(a.nombre, a.tipoMime, a.tamanoBytes, it, a.sha256) }
    },
    editId = report.id
)
