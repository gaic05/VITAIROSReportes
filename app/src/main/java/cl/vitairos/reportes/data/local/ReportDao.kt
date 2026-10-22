package cl.vitairos.reportes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cl.vitairos.reportes.domain.ReportStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {

    @Transaction
    @Query("SELECT * FROM reports ORDER BY creadoEn DESC")
    fun observeAll(): Flow<List<ReportWithDetails>>

    @Transaction
    @Query("SELECT * FROM reports WHERE id = :id")
    fun observeById(id: String): Flow<ReportWithDetails?>

    @Transaction
    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getDetails(id: String): ReportWithDetails?

    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getReport(id: String): ReportEntity?

    @Query("SELECT * FROM attachments WHERE reportId = :id")
    suspend fun getAttachments(id: String): List<AttachmentEntity>

    /** Id de OTRO reporte que ya tiene un archivo con el mismo hash SHA-256 (posible duplicado). */
    @Query("SELECT reportId FROM attachments WHERE sha256 = :sha256 AND reportId != :excludeId LIMIT 1")
    suspend fun reportIdBySha256(sha256: String, excludeId: String): String?

    @Query("SELECT id FROM reports WHERE estado = :estado ORDER BY creadoEn ASC")
    suspend fun idsByStatus(estado: ReportStatus): List<String>

    @Insert
    suspend fun insertReport(report: ReportEntity)

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Insert
    suspend fun insertAttachments(attachments: List<AttachmentEntity>)

    @Query("DELETE FROM attachments WHERE reportId = :id")
    suspend fun deleteAttachmentsOf(id: String)

    @Query("UPDATE attachments SET rutaLocal = NULL WHERE reportId = :id")
    suspend fun clearLocalPaths(id: String)

    @Insert
    suspend fun insertLog(log: LogEntity)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReport(id: String)
}
