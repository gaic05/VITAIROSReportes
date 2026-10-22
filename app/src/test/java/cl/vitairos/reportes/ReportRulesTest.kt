package cl.vitairos.reportes

import cl.vitairos.reportes.domain.ReportRules
import cl.vitairos.reportes.domain.ReportStatus.*
import cl.vitairos.reportes.domain.UserRole.OPERADOR
import cl.vitairos.reportes.domain.UserRole.SUPERVISOR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportRulesTest {

    @Test fun operador_edita_borrador_cola_error_y_correccion() {
        listOf(BORRADOR, EN_COLA, ERROR, REQUIERE_CORRECCION).forEach {
            assertTrue(ReportRules.canEdit(OPERADOR, it))
        }
    }

    @Test fun no_se_edita_mientras_envia_ni_despues_de_sincronizar() {
        assertFalse(ReportRules.canEdit(OPERADOR, ENVIANDO))
        assertFalse(ReportRules.canEdit(OPERADOR, SINCRONIZADO))
    }

    @Test fun supervisor_no_edita_ni_elimina() {
        assertFalse(ReportRules.canEdit(SUPERVISOR, BORRADOR))
        assertFalse(ReportRules.canDelete(SUPERVISOR, BORRADOR))
    }

    @Test fun solo_se_elimina_borrador_o_en_cola() {
        assertTrue(ReportRules.canDelete(OPERADOR, BORRADOR))
        assertTrue(ReportRules.canDelete(OPERADOR, EN_COLA))
        listOf(ENVIANDO, SINCRONIZADO, ERROR, REQUIERE_CORRECCION).forEach {
            assertFalse(ReportRules.canDelete(OPERADOR, it))
        }
    }

    @Test fun reintento_manual_solo_en_error() {
        assertTrue(ReportRules.canRetry(OPERADOR, ERROR))
        assertFalse(ReportRules.canRetry(OPERADOR, EN_COLA))
        assertFalse(ReportRules.canRetry(SUPERVISOR, ERROR))
    }

    @Test fun supervisor_solo_observa_reportes_sincronizados() {
        assertTrue(ReportRules.canObserve(SUPERVISOR, SINCRONIZADO))
        assertFalse(ReportRules.canObserve(SUPERVISOR, EN_COLA))
        assertFalse(ReportRules.canObserve(OPERADOR, SINCRONIZADO))
    }

    @Test fun la_cola_excluye_borradores_y_sincronizados() {
        assertFalse(ReportRules.isInQueue(BORRADOR))
        assertFalse(ReportRules.isInQueue(SINCRONIZADO))
        assertTrue(ReportRules.isInQueue(EN_COLA))
        assertTrue(ReportRules.isInQueue(ENVIANDO))
        assertTrue(ReportRules.isInQueue(ERROR))
    }

    @Test fun etiqueta_de_edicion_segun_estado() {
        assertEquals("Editar reporte", ReportRules.editLabel(EN_COLA))
        assertEquals("Corregir y reenviar", ReportRules.editLabel(ERROR))
    }
}
