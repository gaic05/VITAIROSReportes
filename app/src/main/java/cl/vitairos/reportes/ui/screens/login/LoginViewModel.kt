package cl.vitairos.reportes.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.vitairos.reportes.data.local.SettingsRepository
import cl.vitairos.reportes.di.AppContainer
import cl.vitairos.reportes.domain.DemoAuth
import cl.vitairos.reportes.domain.Session
import cl.vitairos.reportes.domain.UserRole
import cl.vitairos.reportes.domain.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val authError: String? = null
)

class LoginViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, emailError = null, authError = null) }

    fun onPasswordChange(value: String) =
        _state.update { it.copy(password = value, passwordError = null, authError = null) }

    /** Atajo de demostración: solo completa el correo; la clave se escribe (ver README). */
    fun useDemoUser(role: UserRole) =
        _state.update { it.copy(email = DemoAuth.emailDemo(role), emailError = null, authError = null) }

    fun submit(onSuccess: () -> Unit) {
        val s = _state.value
        val emailError = Validators.validateEmail(s.email)
        val passwordError = Validators.validatePassword(s.password)
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        val user = DemoAuth.authenticate(s.email, s.password)
        if (user == null) {
            _state.update { it.copy(password = "", authError = "Correo o contraseña incorrectos.") }
            return
        }
        viewModelScope.launch {
            settingsRepository.setSession(Session(user.email, user.nombre, user.role))
            _state.value = LoginUiState() // no se conserva la contraseña en memoria
            onSuccess()
        }
    }

    companion object {
        fun factory(c: AppContainer) = viewModelFactory {
            initializer { LoginViewModel(c.settingsRepository) }
        }
    }
}
