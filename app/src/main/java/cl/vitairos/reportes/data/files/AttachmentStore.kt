package cl.vitairos.reportes.data.files

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import cl.vitairos.reportes.domain.DraftAttachment
import cl.vitairos.reportes.domain.ReportCatalog
import cl.vitairos.reportes.domain.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.UUID
import kotlin.math.max

sealed interface ImportResult {
    data class Success(val attachment: DraftAttachment) : ImportResult
    data class Rejected(val reason: String) : ImportResult
}

/**
 * Almacenamiento PRIVADO de adjuntos (filesDir): ninguna otra app puede leerlos y no quedan en
 * carpetas públicas. Valida formato/tamaño, comprime imágenes y calcula el hash SHA-256.
 */
class AttachmentStore(private val context: Context) {

    private val dir = File(context.filesDir, "attachments").apply { mkdirs() }
    private val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }

    /** Uri temporal (FileProvider) donde la cámara del sistema deja la foto. */
    fun newCameraUri(): Uri {
        val file = File(cameraDir, "captura_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    suspend fun importUri(uri: Uri): ImportResult = withContext(Dispatchers.IO) { importBlocking(uri) }

    fun delete(paths: List<String?>) {
        paths.filterNotNull().forEach { File(it).delete() }
    }

    private fun importBlocking(uri: Uri): ImportResult {
        val resolver = context.contentResolver
        val nombre = queryName(uri) ?: "archivo"
        val mime = normalizeMime(resolver.getType(uri), nombre)
        if (mime == null) {
            cleanCamera()
            return ImportResult.Rejected("Formato de archivo no reconocido.")
        }
        Validators.validateArchivo(mime, querySize(uri))?.let {
            cleanCamera()
            return ImportResult.Rejected(it)
        }
        val ext = when (mime) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> "pdf"
        }
        val target = File(dir, "${UUID.randomUUID()}.$ext")
        return try {
            val input = resolver.openInputStream(uri) ?: throw IOException("No se pudo abrir el archivo")
            input.use {
                if (mime.startsWith("image/")) saveCompressedImage(it.readBytes(), target, mime)
                else target.outputStream().use { out -> it.copyTo(out) }
            }
            // Verificación posterior a la copia (cubre el caso de tamaño desconocido).
            Validators.validateArchivo(mime, target.length())?.let {
                target.delete()
                return ImportResult.Rejected(it)
            }
            ImportResult.Success(
                DraftAttachment(
                    nombre = nombre,
                    mime = mime,
                    tamanoBytes = target.length(),
                    rutaLocal = target.absolutePath,
                    sha256 = sha256(target)
                )
            )
        } catch (e: Exception) {
            target.delete()
            ImportResult.Rejected("No se pudo procesar el archivo. Intenta nuevamente.")
        } finally {
            cleanCamera()
        }
    }

    /**
     * Reduce el lado mayor a ~2000 px y recomprime (JPEG 85 % / PNG) para bajar el peso sin perder
     * legibilidad. ImageDecoder aplica la orientación EXIF, así las fotos no quedan giradas.
     */
    private fun saveCompressedImage(bytes: ByteArray, target: File, mime: String) {
        val source = ImageDecoder.createSource(ByteBuffer.wrap(bytes))
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val largest = max(info.size.width, info.size.height)
            var sample = 1
            while (largest / sample > MAX_SIDE_PX) sample *= 2
            decoder.setTargetSampleSize(sample)
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        target.outputStream().use { out ->
            if (mime == "image/png") bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            else bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        }
    }

    private fun queryName(uri: Uri): String? =
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }

    private fun querySize(uri: Uri): Long =
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst() && !c.isNull(0)) c.getLong(0) else -1L
        } ?: -1L

    private fun normalizeMime(reported: String?, nombre: String): String? {
        val m = reported?.lowercase()
        if (m != null && m != "application/octet-stream") return if (m == "image/jpg") "image/jpeg" else m
        return when (nombre.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "pdf" -> "application/pdf"
            else -> m
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun cleanCamera() {
        cameraDir.listFiles()?.forEach { it.delete() }
    }

    private companion object {
        const val MAX_SIDE_PX = 2000
        const val JPEG_QUALITY = 85
    }
}
