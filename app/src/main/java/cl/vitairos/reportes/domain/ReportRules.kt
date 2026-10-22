package cl.vitairos.reportes.domain

import cl.vitairos.reportes.domain.ReportStatus.*

/** Qué puede hacer cada rol según el estado del reporte (respuestas del cuestionario, sección 3). */
object ReportRules {
    private val EDITABLES = setOf(BORRADOR, EN_COLA, ERROR, REQUIERE_CORRECCION)
    private val ELIMINABLES = setOf(BORRADOR, EN_COLA)
    private val ESTADOS_COLA = setOf(EN_COLA, ENVIANDO, ERROR, REQUIERE_CORRECCION)

    fun canEdit(role: UserRole, status: ReportStatus) = role == UserRole.OPERADOR && status in EDITABLES
    fun canDelete(role: UserRole, status: ReportStatus) = role == UserRole.OPERADOR && status in ELIMINABLES
    fun canRetry(role: UserRole, status: ReportStatus) = role == UserRole.OPERADOR && status == ERROR
    fun canObserve(role: UserRole, status: ReportStatus) = role == UserRole.SUPERVISOR && status == SINCRONIZADO

    /** Pertenece a la pantalla "Cola de envíos" (los borradores solo se ven en Historial). */
    fun isInQueue(status: ReportStatus) = status in ESTADOS_COLA

    fun editLabel(status: ReportStatus) =
        if (status == ERROR || status == REQUIERE_CORRECCION) "Corregir y reenviar" else "Editar reporte"
}
