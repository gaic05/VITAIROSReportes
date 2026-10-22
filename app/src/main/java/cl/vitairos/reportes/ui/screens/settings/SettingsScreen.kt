package cl.vitairos.reportes.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cl.vitairos.reportes.domain.AppSettings
import cl.vitairos.reportes.domain.Session
import cl.vitairos.reportes.ui.LocalThemeController
import cl.vitairos.reportes.ui.components.BigOutlinedButton
import cl.vitairos.reportes.ui.components.KeyValueRow
import cl.vitairos.reportes.ui.components.ScreenScaffold
import cl.vitairos.reportes.ui.components.SectionLabel
import cl.vitairos.reportes.ui.components.SurfaceCard
import cl.vitairos.reportes.ui.components.ThemeToggleButton

@Composable
fun SettingsScreen(
    session: Session,
    settings: AppSettings,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onLogout: () -> Unit,
    onSimulateOffline: (Boolean) -> Unit,
    onSimulateApiFailure: (Boolean) -> Unit
) {
    val theme = LocalThemeController.current
    // Maqueta: este interruptor aún no se guarda ni envía notificaciones reales.
    var avisos by rememberSaveable { mutableStateOf(true) }

    ScreenScaffold(title = "Ajustes", onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionLabel("Cuenta")
            SurfaceCard {
                KeyValueRow("Usuario", session.nombre)
                KeyValueRow("Perfil", session.role.label)
            }
            BigOutlinedButton(text = "Ver mi perfil", onClick = onOpenProfile)

            SectionLabel("Apariencia")
            SurfaceCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable(onClick = theme.toggle),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Modo claro / oscuro", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (theme.isDark) "Modo oscuro activo" else "Modo claro activo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ThemeToggleButton(isDark = theme.isDark, onToggle = theme.toggle)
                }
            }

            SectionLabel("Preferencias")
            SurfaceCard {
                SwitchRow(
                    title = "Avisos de sincronización",
                    subtitle = "Te avisa cuando un reporte se envía o falla (maqueta: aún no se guarda).",
                    checked = avisos,
                    onChange = { avisos = it }
                )
                KeyValueRow("Idioma", "Español (Chile)")
            }

            SectionLabel("Simulación (solo prototipo)")
            SurfaceCard {
                SwitchRow(
                    title = "Simular sin conexión",
                    subtitle = "Fuerza el modo offline para probar la cola y la sincronización automática.",
                    checked = settings.simulateOffline,
                    onChange = onSimulateOffline
                )
                SwitchRow(
                    title = "Simular falla de la API",
                    subtitle = "La API mock responde con error: se ve el backoff de 5, 15 y 30 s y luego el estado Error.",
                    checked = settings.simulateApiFailure,
                    onChange = onSimulateApiFailure
                )
            }

            SectionLabel("Acerca de")
            SurfaceCard {
                KeyValueRow("Aplicación", "VITAIROS Reportes")
                KeyValueRow("Versión", "0.1 (prototipo)")
            }
            Text(
                "Los envíos usan una API simulada (mock) de VITAIROS Industrial AI. No se usan datos ni credenciales reales. " +
                        "Prototipo académico DSY1105.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            BigOutlinedButton(text = "Cerrar sesión", onClick = onLogout, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}
