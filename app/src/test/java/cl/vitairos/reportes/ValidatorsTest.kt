package cl.vitairos.reportes

import cl.vitairos.reportes.domain.ReportCatalog
import cl.vitairos.reportes.domain.ReportForm
import cl.vitairos.reportes.domain.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    private val formBase = ReportForm(tipo = "Mantención", proceso = "Chancado", area = "Planta")

    @Test fun email_vacio_da_error() = assertEquals("Ingresa tu correo", Validators.validateEmail("  "))
    @Test fun email_sin_arroba_da_error() = assertEquals("Correo no válido", Validators.validateEmail("operador.vitairos.test"))
    @Test fun email_valido_no_da_error() = assertNull(Validators.validateEmail("operador@vitairos.test"))
    @Test fun clave_corta_da_error() = assertEquals("Mínimo 6 caracteres", Validators.validatePassword("abc"))
    @Test fun clave_valida_no_da_error() = assertNull(Validators.validatePassword("Operador2026"))

    @Test fun formulario_vacio_marca_tipo_proceso_y_area() {
        val e = Validators.validateDatos(ReportForm())
        assertTrue(e.hasErrors)
        assertEquals("Selecciona un tipo de reporte", e.tipo)
        assertEquals("Selecciona un proceso asociado", e.proceso)
        assertEquals("Selecciona el área / faena", e.area)
        assertNull(e.turno)
    }

    @Test fun formulario_completo_no_tiene_errores() {
        assertFalse(Validators.validateDatos(formBase).hasErrors)
    }

    @Test fun turno_es_obligatorio_solo_en_reporte_de_turno() {
        val conTurnoVacio = formBase.copy(tipo = ReportCatalog.TIPO_TURNO)
        assertEquals("El turno es obligatorio en reportes de turno", Validators.validateDatos(conTurnoVacio).turno)
        assertNull(Validators.validateDatos(conTurnoVacio.copy(turno = "A")).turno)
        assertNull(Validators.validateDatos(formBase).turno)
    }

    @Test fun inspeccion_de_seguridad_exige_observacion_de_10_caracteres() {
        val f = formBase.copy(tipo = ReportCatalog.TIPO_SEGURIDAD, observaciones = "corta")
        assertEquals("Describe el hallazgo (mínimo 10 caracteres)", Validators.validateDatos(f).observaciones)
        assertNull(Validators.validateDatos(f.copy(observaciones = "Extintor sin sello")).observaciones)
    }

    @Test fun observacion_no_puede_superar_280_caracteres() {
        val f = formBase.copy(observaciones = "x".repeat(281))
        assertEquals("Máximo 280 caracteres", Validators.validateDatos(f).observaciones)
        assertNull(Validators.validateDatos(f.copy(observaciones = "x".repeat(280))).observaciones)
    }

    @Test fun sin_adjuntos_no_permite_continuar() {
        assertEquals("Agrega al menos un adjunto.", Validators.validateAdjuntos(formBase))
    }

    @Test fun formato_no_permitido_se_rechaza() {
        assertEquals("Formato no permitido. Usa JPG, PNG o PDF.", Validators.validateArchivo("image/webp", 1000))
        assertEquals("Formato no permitido. Usa JPG, PNG o PDF.", Validators.validateArchivo(null, 1000))
    }

    @Test fun archivo_vacio_se_rechaza() {
        assertEquals("El archivo está vacío.", Validators.validateArchivo("application/pdf", 0))
    }

    @Test fun archivo_sobre_10_mb_se_rechaza_informando_el_tamano() {
        val doce = 12L * 1024 * 1024
        assertEquals(
            "El archivo pesa 12.0 MB y supera el límite de 10 MB. No se adjuntó.",
            Validators.validateArchivo("image/png", doce)
        )
    }

    @Test fun archivo_valido_se_acepta() {
        assertNull(Validators.validateArchivo("image/jpeg", 2L * 1024 * 1024))
        assertNull(Validators.validateArchivo("image/png", -1))
        assertNull(Validators.validateArchivo("application/pdf", ReportCatalog.MAX_ARCHIVO_BYTES))
    }

    @Test fun revision_exige_ambas_confirmaciones() {
        assertNull(Validators.validateRevision(legible = true, bienOrientado = true))
        assertEquals(
            "Confirma que el documento es legible y está bien orientado.",
            Validators.validateRevision(legible = true, bienOrientado = false)
        )
    }
}
