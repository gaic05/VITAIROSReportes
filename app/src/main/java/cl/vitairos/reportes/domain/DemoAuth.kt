package cl.vitairos.reportes.domain

import java.security.MessageDigest

object PasswordHasher {
    fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}

data class DemoUser(val email: String, val nombre: String, val role: UserRole, val passwordHash: String)

/**
 * Login simulado y local con usuarios ficticios. Solo se guarda el hash de las claves
 * (no hay contraseñas en texto plano en el código). Las claves de demostración están en el README.
 */
object DemoAuth {
    val users = listOf(
        DemoUser("operador@vitairos.test", "Operador demo", UserRole.OPERADOR, "0bbcfe3571f1f904825f3556dd8e079eb003231640141b1ac07f8c169312d57f"),
        DemoUser("supervisor@vitairos.test", "Supervisor demo", UserRole.SUPERVISOR, "2b8f1a15a8c8eb4d6b339121e6cce19839b60688f0dc43f45ce62bcafa317c96")
    )

    fun emailDemo(role: UserRole): String = users.first { it.role == role }.email

    fun authenticate(email: String, password: String): DemoUser? {
        val hash = PasswordHasher.sha256(password)
        return users.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) && it.passwordHash == hash }
    }
}
