package cl.vitairos.reportes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/** Alertas (calidad de imagen, posible duplicado) de un adjunto. No bloquean: solo informan. */
@Composable
fun AttachmentWarnings(messages: List<String>?) {
    if (messages.isNullOrEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        messages.forEach {
            Text("⚠ $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}