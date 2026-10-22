package cl.vitairos.reportes.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val localeCl = Locale("es", "CL")

fun formatDateTime(millis: Long): String = SimpleDateFormat("dd/MM/yyyy HH:mm", localeCl).format(Date(millis))

fun formatTime(millis: Long): String = SimpleDateFormat("HH:mm:ss", localeCl).format(Date(millis))

fun formatSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> String.format(localeCl, "%.1f MB", bytes / (1024.0 * 1024.0))
    bytes >= 1024 -> String.format(localeCl, "%.0f KB", bytes / 1024.0)
    else -> "$bytes B"
}
