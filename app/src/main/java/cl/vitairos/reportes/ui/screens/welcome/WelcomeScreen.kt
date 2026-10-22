package cl.vitairos.reportes.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.vitairos.reportes.R
import cl.vitairos.reportes.ui.components.BigButton
import cl.vitairos.reportes.ui.theme.Cobre
import cl.vitairos.reportes.ui.theme.Hueso
import cl.vitairos.reportes.ui.theme.Oro
import cl.vitairos.reportes.ui.theme.VerdeMineral
import cl.vitairos.reportes.ui.theme.VerdeNoche
import cl.vitairos.reportes.ui.theme.VerdeProfundo

/** Primera pantalla de la app: marca, para qué sirve y botón "Ingresar" (lleva al login). */
@Composable
fun WelcomeScreen(onEnter: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Bloque de marca: siempre verde noche, con las esquinas inferiores redondeadas.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(VerdeNoche)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier
                    .size(104.dp)
                    .background(VerdeProfundo, RoundedCornerShape(24.dp))
                    .border(1.dp, Oro.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(14.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.isotipo),
                    contentDescription = "Isotipo VITAIROS",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                "VITAIROS",
                style = MaterialTheme.typography.headlineMedium,
                color = Hueso,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                "INDUSTRIAL AI",
                style = MaterialTheme.typography.labelLarge,
                color = Oro,
                letterSpacing = 3.sp
            )
            Box(
                Modifier
                    .padding(top = 6.dp)
                    .width(48.dp)
                    .height(3.dp)
                    .background(Cobre, RoundedCornerShape(2.dp))
            )
        }

        // Descripción y botón.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Información operativa convertida en datos utilizables.",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Start
            )
            Text(
                "Esta app lleva esa idea a terreno: registras lo que ocurre en tu operación, " +
                        "adjuntas el documento y queda ordenado para integrarse con los procesos y sistemas de tu organización. " +
                        "Si no hay señal, el reporte espera en una cola y se envía solo cuando vuelve la conexión.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                Step("1", "Captura", "Foto o PDF del documento, en terreno.")
                Step("2", "Registra", "Tipo, proceso, área y turno, ordenados desde el origen.")
                Step("3", "Envía", "Se sincroniza solo cuando hay conexión.")
            }

            BigButton(text = "Ingresar", onClick = onEnter, modifier = Modifier.padding(top = 8.dp))

            Text(
                "VITAIROS · IA aplicada e ingeniería digital\nPrototipo académico · versión 0.1",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Step(number: String, title: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(VerdeMineral),
            contentAlignment = Alignment.Center
        ) {
            Text(number, style = MaterialTheme.typography.titleMedium, color = Hueso)
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
