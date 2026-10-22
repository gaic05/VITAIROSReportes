package cl.vitairos.reportes.data

import androidx.room.withTransaction
import cl.vitairos.reportes.data.files.AttachmentStore
import cl.vitairos.reportes.data.local.AppDatabase
import cl.vitairos.reportes.data.local.LogEntity
import cl.vitairos.reportes.data.local.ReportEntity
import cl.vitairos.reportes.data.local.ReportWithDetails
import cl.vitairos.reportes.data.local.toEntity
import cl.vitairos.reportes.data.network.MockApi
import cl.vitairos.reportes.domain.ReportForm
import cl.vitairos.reportes.domain.ReportIdGenerator
import cl.vitairos.reportes.domain.ReportRules
import cl.vitairos.reportes.domain.ReportStatus
import cl.vitairos.reportes.domain.SyncDecision
import cl.vitairos.reportes.domain.SyncPolicy
import cl.vitairos.reportes.domain.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface SyncOutcome {
    /** El reporte ya no está "En cola" (se editó, eliminó o ya se envió): no se hace nada. */
    data object Skipped : SyncOutcome
    data object Success : SyncOutcome
    data class Failed(val decision: SyncDecision) : SyncOutcome
}

/**
 * Única puerta de acceso a los datos de reportes. Los ViewModels nunca hablan con Room,
 * con el sistema de archivos ni con la API directamente.
 */
class ReportRepository(
    private val database: AppDatabase,
    private val store: AttachmentStore,
    private val api: MockApi,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val dao = database.reportDao()

    fun observeAll(): Flow<List<ReportWithDetails>> = dao.observeAll().map { list -> list.map { it.ordenado() } }

    fun observe(id: String): Flow<ReportWithDetails?> = dao.observeById(id).map { it?.ordenado() }

    suspend fun get(id: String): ReportWithDetails? = dao.getDetails(id)?.ordenado()

    /** Id de otro reporte (distinto de [excludeReportId]) que ya contiene el mismo archivo, o null. */
    suspend fun findReportWithSameFile(sha256: String, excludeReportId: String?): String? =
        dao.reportIdBySha256(sha256, excludeReportId ?: "")

    private fun ReportWithDetails.ordenado() = copy(
        attachments = attachments.sortedBy { it.id },
        log = log.sortedWith(compareBy({ it.timestamp }, { it.id }))
    )

    /** Crea o actualiza un reporte (según form.editId). Devuelve el id. Todo en una transacción. */
    suspend fun saveReport(form: ReportForm, usuario: String, enqueue: Boolean): String {
        val now = clock()
        val estado = if (enqueue) ReportStatus.EN_COLA else ReportStatus.BORRADOR
        var archivosAEliminar: List<String?> = emptyList()

        val id = database.withTransaction {
            val editId = form.editId
            if (editId == null) {
                val nuevoId = ReportIdGenerator.newId()
                dao.insertReport(
                    ReportEntity(
                        id = nuevoId, tipo = form.tipo, proceso = form.proceso, area = form.area,
                        turno = form.turno, observaciones = form.observaciones.trim(), usuario = usuario,
                        creadoEn = now, actualizadoEn = now, estado = estado, intentos = 0, version = 1,
                        mensaje = "", sincronizadoEn = null
                    )
                )
                dao.insertAttachments(form.adjuntos.map { it.toEntity(nuevoId) })
                dao.insertLog(
                    LogEntity(
                        reportId = nuevoId, timestamp = now,
                        texto = if (enqueue) "Reporte creado y puesto en cola" else "Borrador creado en el dispositivo"
                    )
                )
                nuevoId
            } else {
                val actual = dao.getReport(editId) ?: error("El reporte $editId ya no existe")
                check(ReportRules.canEdit(UserRole.OPERADOR, actual.estado)) {
                    "El reporte $editId no puede editarse en estado ${actual.estado.label}"
                }
                // Corregir un Error / Requiere corrección genera una nueva versión (trazabilidad).
                val version = if (actual.estado == ReportStatus.ERROR || actual.estado == ReportStatus.REQUIERE_CORRECCION)
                    actual.version + 1 else actual.version
                dao.updateReport(
                    actual.copy(
                        tipo = form.tipo, proceso = form.proceso, area = form.area, turno = form.turno,
                        observaciones = form.observaciones.trim(), estado = estado, intentos = 0,
                        version = version, mensaje = "", actualizadoEn = now
                    )
                )
                val conservadas = form.adjuntos.map { it.rutaLocal }.toSet()
                archivosAEliminar = dao.getAttachments(editId).map { it.rutaLocal }.filter { it !in conservadas }
                dao.deleteAttachmentsOf(editId)
                dao.insertAttachments(form.adjuntos.map { it.toEntity(editId) })
                dao.insertLog(
                    LogEntity(
                        reportId = editId, timestamp = now,
                        texto = "Reporte editado" + (if (enqueue) " y puesto en cola" else "") +
                            (if (version > actual.version) " (versión $version)" else "")
                    )
                )
                editId
            }
        }
        store.delete(archivosAEliminar)
        return id
    }

    /** Elimina un reporte (solo Borrador o En cola). Devuelve false si el estado no lo permite. */
    suspend fun deleteReport(id: String): Boolean {
        val details = dao.getDetails(id) ?: return false
        if (!ReportRules.canDelete(UserRole.OPERADOR, details.report.estado)) return false
        database.withTransaction { dao.deleteReport(id) }
        store.delete(details.attachments.map { it.rutaLocal })
        return true
    }

    /** Reintento manual: vuelve a la cola con el contador de intentos en 0 (el historial se conserva). */
    suspend fun requeueForRetry(id: String): Boolean = database.withTransaction {
        val r = dao.getReport(id) ?: return@withTransaction false
        if (!ReportRules.canRetry(UserRole.OPERADOR, r.estado)) return@withTransaction false
        val now = clock()
        dao.updateReport(r.copy(estado = ReportStatus.EN_COLA, intentos = 0, mensaje = "", actualizadoEn = now))
        dao.insertLog(LogEntity(reportId = id, timestamp = now, texto = "Reintento manual solicitado"))
        true
    }

    /** Rol Supervisor: marca un reporte sincronizado como "Requiere corrección". */
    suspend fun markObserved(id: String): Boolean = database.withTransaction {
        val r = dao.getReport(id) ?: return@withTransaction false
        if (!ReportRules.canObserve(UserRole.SUPERVISOR, r.estado)) return@withTransaction false
        val now = clock()
        dao.updateReport(
            r.copy(
                estado = ReportStatus.REQUIERE_CORRECCION, actualizadoEn = now,
                mensaje = "Observado por Supervisor: requiere corrección."
            )
        )
        dao.insertLog(LogEntity(reportId = id, timestamp = now, texto = "Supervisor marcó el reporte como observado"))
        true
    }

    suspend fun queuedIds(): List<String> = dao.idsByStatus(ReportStatus.EN_COLA)

    /** Al abrir la app: lo que quedó "Enviando" por un cierre inesperado vuelve a la cola. */
    suspend fun recoverInterrupted() {
        dao.idsByStatus(ReportStatus.ENVIANDO).forEach { id ->
            database.withTransaction {
                val r = dao.getReport(id) ?: return@withTransaction
                val now = clock()
                dao.updateReport(r.copy(estado = ReportStatus.EN_COLA, actualizadoEn = now))
                dao.insertLog(LogEntity(reportId = id, timestamp = now, texto = "Envío interrumpido · vuelve a la cola"))
            }
        }
    }

    /** Ejecuta UN intento de envío contra la API mock y deja el resultado registrado. */
    suspend fun attemptSync(id: String): SyncOutcome {
        val details = dao.getDetails(id) ?: return SyncOutcome.Skipped
        val inicial = details.report
        if (inicial.estado != ReportStatus.EN_COLA) return SyncOutcome.Skipped

        val intento = inicial.intentos + 1
        val t0 = clock()
        database.withTransaction {
            dao.updateReport(inicial.copy(estado = ReportStatus.ENVIANDO, intentos = intento, mensaje = "", actualizadoEn = t0))
            dao.insertLog(LogEntity(reportId = id, timestamp = t0, texto = "Enviando (intento $intento)"))
        }

        val respuesta = api.enviarReporte(details)
        val t1 = clock()

        if (respuesta.ok) {
            database.withTransaction {
                val r = dao.getReport(id) ?: return@withTransaction
                dao.updateReport(
                    r.copy(estado = ReportStatus.SINCRONIZADO, mensaje = "", sincronizadoEn = t1, actualizadoEn = t1)
                )
                dao.insertLog(LogEntity(reportId = id, timestamp = t1, texto = "Sincronizado · ${respuesta.message}"))
                dao.clearLocalPaths(id) // el archivo ya no es necesario en el teléfono
            }
            store.delete(details.attachments.map { it.rutaLocal })
            return SyncOutcome.Success
        }

        val decision = SyncPolicy.afterFailure(intento)
        database.withTransaction {
            val r = dao.getReport(id) ?: return@withTransaction
            when (decision) {
                is SyncDecision.RetryIn -> {
                    val msg = "Intento $intento fallido. Reintento automático en ${decision.seconds} s."
                    dao.updateReport(r.copy(estado = ReportStatus.EN_COLA, mensaje = msg, actualizadoEn = t1))
                    dao.insertLog(LogEntity(reportId = id, timestamp = t1, texto = "Intento $intento fallido · reintento en ${decision.seconds} s"))
                }
                SyncDecision.GiveUp -> {
                    val msg = "Error tras $intento intentos (3 reintentos automáticos). Pendiente de reintento manual."
                    dao.updateReport(r.copy(estado = ReportStatus.ERROR, mensaje = msg, actualizadoEn = t1))
                    dao.insertLog(LogEntity(reportId = id, timestamp = t1, texto = "Intento $intento fallido · estado Error"))
                }
            }
        }
        return SyncOutcome.Failed(decision)
    }
}
