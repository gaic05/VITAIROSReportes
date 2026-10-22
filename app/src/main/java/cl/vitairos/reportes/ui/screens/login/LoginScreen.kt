package cl.vitairos.reportes.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.vitairos.reportes.R
import cl.vitairos.reportes.domain.DemoAuth
import cl.vitairos.reportes.domain.UserRole
import cl.vitairos.reportes.ui.LocalThemeController
import cl.vitairos.reportes.ui.components.BigButton
import cl.vitairos.reportes.ui.components.InlineMessage
import cl.vitairos.reportes.ui.components.SectionLabel
import cl.vitairos.reportes.ui.components.ThemeToggleButton
import cl.vitairos.reportes.ui.components.ValidatedTextField
import cl.vitairos.reportes.ui.theme.VerdeNoche
import androidx.compose.foundation.layout.statusBars

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoggedIn: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val theme = LocalThemeController.current
    val focus = LocalFocusManager.current

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Franja verde bajo la barra de estado (igual que la barra superior del resto de pantallas).
        Spacer(
            Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(VerdeNoche)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                ThemeToggleButton(isDark = theme.isDark, onToggle = theme.toggle)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    Modifier
                        .size(68.dp)
                        .background(VerdeNoche, RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.isotipo),
                        contentDescription = "Isotipo VITAIROS",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column {
                    Text("VITAIROS", style = MaterialTheme.typography.headlineSmall, letterSpacing = 1.sp)
                    Text(
                        "INDUSTRIAL AI",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                "Reportes de terreno, sin perder contexto.",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                "Captura el documento, registra sus datos y envíalo cuando haya conexión.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionLabel("Usuario de demostración", Modifier.padding(top = 8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                UserRole.entries.forEach { role ->
                    FilterChip(
                        selected = state.email.trim().equals(DemoAuth.emailDemo(role), ignoreCase = true),
                        onClick = { viewModel.useDemoUser(role) },
                        label = { Text(role.label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            ValidatedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = "Correo (ficticio)",
                error = state.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) })
            )
            ValidatedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                error = state.passwordError,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focus.clearFocus()
                    viewModel.submit(onLoggedIn)
                })
            )

            InlineMessage(state.authError)

            BigButton(text = "Ingresar", onClick = {
                focus.clearFocus()
                viewModel.submit(onLoggedIn)
            })

            Text(
                "Sesión simulada local. No se usan credenciales reales.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
