package cl.vitairos.reportes.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.vitairos.reportes.domain.AppSettings
import cl.vitairos.reportes.domain.Session
import cl.vitairos.reportes.domain.ThemeMode
import cl.vitairos.reportes.domain.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "vitairos_settings")

/**
 * Preferencias simples en DataStore (recomendado por VITAIROS para ajustes):
 * tema claro/oscuro, sesión simulada (sin contraseña) y los interruptores de simulación.
 */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val EMAIL = stringPreferencesKey("session_email")
        val NAME = stringPreferencesKey("session_name")
        val ROLE = stringPreferencesKey("session_role")
        val SIM_OFFLINE = booleanPreferencesKey("sim_offline")
        val SIM_API_FAIL = booleanPreferencesKey("sim_api_fail")
    }

    val settings: Flow<AppSettings> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            val email = prefs[Keys.EMAIL]
            val name = prefs[Keys.NAME]
            val role = prefs[Keys.ROLE]?.let { r -> runCatching { UserRole.valueOf(r) }.getOrNull() }
            AppSettings(
                themeMode = runCatching { ThemeMode.valueOf(prefs[Keys.THEME] ?: "") }
                    .getOrDefault(ThemeMode.SISTEMA),
                session = if (email != null && name != null && role != null) Session(email, name, role) else null,
                simulateOffline = prefs[Keys.SIM_OFFLINE] ?: false,
                simulateApiFailure = prefs[Keys.SIM_API_FAIL] ?: false
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setSession(session: Session?) {
        dataStore.edit { prefs ->
            if (session == null) {
                prefs.remove(Keys.EMAIL)
                prefs.remove(Keys.NAME)
                prefs.remove(Keys.ROLE)
            } else {
                prefs[Keys.EMAIL] = session.email
                prefs[Keys.NAME] = session.nombre
                prefs[Keys.ROLE] = session.role.name
            }
        }
    }

    suspend fun setSimulateOffline(value: Boolean) {
        dataStore.edit { it[Keys.SIM_OFFLINE] = value }
    }

    suspend fun setSimulateApiFailure(value: Boolean) {
        dataStore.edit { it[Keys.SIM_API_FAIL] = value }
    }
}
