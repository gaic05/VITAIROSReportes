package cl.vitairos.reportes

import cl.vitairos.reportes.domain.SyncDecision
import cl.vitairos.reportes.domain.SyncPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncPolicyTest {

    @Test fun backoff_es_5_15_y_30_segundos() {
        assertEquals(SyncDecision.RetryIn(5), SyncPolicy.afterFailure(1))
        assertEquals(SyncDecision.RetryIn(15), SyncPolicy.afterFailure(2))
        assertEquals(SyncDecision.RetryIn(30), SyncPolicy.afterFailure(3))
    }

    @Test fun tras_el_cuarto_intento_fallido_queda_en_error() {
        assertEquals(SyncDecision.GiveUp, SyncPolicy.afterFailure(4))
        assertEquals(SyncDecision.GiveUp, SyncPolicy.afterFailure(10))
    }

    @Test fun hay_exactamente_3_reintentos_automaticos() {
        val reintentos = (1..10).count { SyncPolicy.afterFailure(it) is SyncDecision.RetryIn }
        assertEquals(3, reintentos)
    }
}
