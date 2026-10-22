package cl.vitairos.reportes

import cl.vitairos.reportes.domain.DemoAuth
import cl.vitairos.reportes.domain.ReportIdGenerator
import cl.vitairos.reportes.domain.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoAuthTest {

    @Test fun operador_con_clave_correcta_ingresa() {
        val u = DemoAuth.authenticate("operador@vitairos.test", "Operador2026")
        assertNotNull(u)
        assertEquals(UserRole.OPERADOR, u!!.role)
    }

    @Test fun supervisor_con_clave_correcta_ingresa() {
        assertEquals(UserRole.SUPERVISOR, DemoAuth.authenticate(" Supervisor@vitairos.test ", "Supervisor2026")!!.role)
    }

    @Test fun clave_incorrecta_no_ingresa() {
        assertNull(DemoAuth.authenticate("operador@vitairos.test", "otraClave"))
    }

    @Test fun la_clave_de_un_usuario_no_sirve_para_el_otro() {
        assertNull(DemoAuth.authenticate("operador@vitairos.test", "Supervisor2026"))
    }

    @Test fun usuario_inexistente_no_ingresa() {
        assertNull(DemoAuth.authenticate("alguien@vitairos.test", "Operador2026"))
    }

    @Test fun ids_de_reporte_tienen_formato_y_son_unicos() {
        val ids = List(500) { ReportIdGenerator.newId() }
        assertTrue(ids.all { ReportIdGenerator.isValid(it) })
        assertEquals(500, ids.toSet().size)
        assertFalse(ReportIdGenerator.isValid("VTR-123"))
    }
}
