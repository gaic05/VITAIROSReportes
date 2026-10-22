package cl.vitairos.reportes.ui.screens.report

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.vitairos.reportes.R
import cl.vitairos.reportes.domain.ReportCatalog
import cl.vitairos.reportes.ui.components.AttachmentThumb
import cl.vitairos.reportes.ui.components.AttachmentWarnings
import cl.vitairos.reportes.ui.components.BigButton
import cl.vitairos.reportes.ui.components.InlineMessage
import cl.vitairos.reportes.ui.components.ScreenScaffold
import cl.vitairos.reportes.ui.components.SectionLabel
import cl.vitairos.reportes.ui.components.SurfaceCard
import cl.vitairos.reportes.ui.formatSize

/**
 * Paso 2 de 3: adjuntos. Recursos nativos usados:
 *  - Cámara (TakePicture + permiso CAMERA en tiempo de ejecución).
 *  - Galería (Photo Picker del sistema: no necesita permisos de almacenamiento).
 *  - Documentos PDF (selector de archivos del sistema, SAF: tampoco necesita permisos).
 */
@Composable
fun AttachScreen(
    viewModel: ReportFormViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var cameraDenied by rememberSaveable { mutableStateOf(false) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) cameraUri?.let { viewModel.addFromUri(it) }
    }

    fun launchCamera() {
        val uri = viewModel.newCameraUri()
        cameraUri = uri
        takePicture.launch(uri)
    }

    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            cameraDenied = false
            launchCamera()
        } else {
            cameraDenied = true
        }
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.addFromUri(uri)
    }
    val pickPdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.addFromUri(uri)
    }

    val adjuntos = state.form.adjuntos

    ScreenScaffold(title = if (state.form.editId != null) "Editar reporte" else "Nuevo reporte", onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionLabel("Paso 2 de 3 · Adjuntos (${adjuntos.size}/${ReportCatalog.MAX_ADJUNTOS})")
            Text(
                "Fotografía el documento o selecciónalo. Formatos JPG, PNG o PDF · máximo 10 MB por archivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SurfaceCard {
                Text("Antes de adjuntar", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Usa solo documentos sintéticos o autorizados. No fotografíes personas, credenciales, " +
                            "pantallas con información sensible ni documentación productiva.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SourceButton("Cámara", R.drawable.ic_camera, Modifier.weight(1f)) {
                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED
                    if (granted) launchCamera() else cameraPermission.launch(Manifest.permission.CAMERA)
                }
                SourceButton("Galería", R.drawable.ic_image, Modifier.weight(1f)) {
                    pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                SourceButton("PDF", R.drawable.ic_pdf, Modifier.weight(1f)) {
                    pickPdf.launch(arrayOf("application/pdf"))
                }
            }

            AnimatedVisibility(visible = cameraDenied) {
                SurfaceCard {
                    Text(
                        "El permiso de cámara fue denegado. Puedes adjuntar desde la galería o un PDF, o habilitar la cámara en los ajustes de la app.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        )
                    }) { Text("Abrir ajustes de la app") }
                }
            }

            if (state.importing) LinearProgressIndicator(Modifier.fillMaxWidth())
            InlineMessage(state.attachError)

            adjuntos.forEachIndexed { index, a ->
                SurfaceCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AttachmentThumb(path = a.rutaLocal, mime = a.mime, size = 64.dp)
                        Column(Modifier.weight(1f)) {
                            Text(a.nombre, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                            Text(
                                "${formatSize(a.tamanoBytes)} · ${a.mime.substringAfter('/').uppercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AttachmentWarnings(state.warnings[a.rutaLocal])
                        }
                        IconButton(onClick = { viewModel.removeAttachment(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Quitar ${a.nombre}", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            BigButton(
                text = "Continuar a revisión",
                onClick = { if (viewModel.validateAdjuntos()) onNext() }
            )
        }
    }
}

@Composable
private fun SourceButton(label: String, iconRes: Int, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 76.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(painterResource(iconRes), contentDescription = null)
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
