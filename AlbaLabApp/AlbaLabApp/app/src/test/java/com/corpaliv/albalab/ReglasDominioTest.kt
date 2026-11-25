package com.corpaliv.albalab

import com.corpaliv.albalab.domain.ReglasGestion
import com.corpaliv.albalab.domain.model.EstadoSolicitud
import com.corpaliv.albalab.domain.model.TipoColaboracion
import com.corpaliv.albalab.domain.validation.Validadores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasDominioTest {

    // ---- Estados (deben coincidir con ms-vinculacion) ----

    @Test
    fun flujo_de_estados_igual_al_del_servidor() {
        assertEquals(listOf(EstadoSolicitud.EN_REVISION, EstadoSolicitud.DESCARTADO), EstadoSolicitud.RECIBIDA.siguientes())
        assertEquals(listOf(EstadoSolicitud.CONTACTADO, EstadoSolicitud.DESCARTADO), EstadoSolicitud.EN_REVISION.siguientes())
        assertEquals(listOf(EstadoSolicitud.CONCRETADO, EstadoSolicitud.DESCARTADO), EstadoSolicitud.CONTACTADO.siguientes())
        assertTrue(EstadoSolicitud.CONCRETADO.siguientes().isEmpty())
        assertTrue(EstadoSolicitud.DESCARTADO.siguientes().isEmpty())
    }

    @Test
    fun estado_desde_nombre() {
        assertEquals(EstadoSolicitud.CONTACTADO, EstadoSolicitud.desdeNombre("CONTACTADO"))
        assertNull(EstadoSolicitud.desdeNombre("contactado"))
        assertNull(EstadoSolicitud.desdeNombre(null))
        assertTrue(EstadoSolicitud.entries.all { it.etiqueta.isNotBlank() && it.descripcion.isNotBlank() })
    }

    // ---- Modalidades de colaboración (catálogo de la propuesta) ----

    @Test
    fun modalidades_coinciden_con_el_catalogo_de_la_propuesta() {
        assertEquals(
            listOf("DONACION", "ALIANZA_RSE", "VOLUNTARIADO", "COMPRA", "CAPACITACION", "OTRO"),
            TipoColaboracion.entries.map { it.name }
        )
        assertEquals(TipoColaboracion.ALIANZA_RSE, TipoColaboracion.desdeNombre("ALIANZA_RSE"))
        assertNull(TipoColaboracion.desdeNombre("ALIANZA"))
        assertTrue(TipoColaboracion.DONACION.requiereMonto)
        assertFalse(TipoColaboracion.OTRO.requiereMonto)
    }

    // ---- Observación al cambiar de estado ----

    @Test
    fun descartar_exige_observacion() {
        assertNotNull(ReglasGestion.errorObservacion(EstadoSolicitud.DESCARTADO, "  "))
        assertNull(ReglasGestion.errorObservacion(EstadoSolicitud.DESCARTADO, "Sin respuesta"))
        assertNull(ReglasGestion.errorObservacion(EstadoSolicitud.EN_REVISION, ""))
    }

    @Test
    fun observacion_no_puede_superar_el_maximo_ni_faltar_el_estado() {
        assertNotNull(ReglasGestion.errorObservacion(EstadoSolicitud.CONTACTADO, "a".repeat(501)))
        assertNull(ReglasGestion.errorObservacion(EstadoSolicitud.CONTACTADO, "a".repeat(500)))
        assertNotNull(ReglasGestion.errorObservacion(null, "x"))
    }

    // ---- Código público ----

    @Test
    fun formato_del_codigo_de_solicitud() {
        assertNull(ReglasGestion.errorCodigo("AL-ABCD2345"))
        assertNull(ReglasGestion.errorCodigo(" al-abcd2345 "))
        assertNotNull(ReglasGestion.errorCodigo(""))
        assertNotNull(ReglasGestion.errorCodigo("AL-ABC"))
        assertNotNull(ReglasGestion.errorCodigo("AL-ABCD0OI1")) // caracteres ambiguos no existen en los códigos
        assertNotNull(ReglasGestion.errorCodigo("XX-ABCD2345"))
    }

    // ---- Contacto: al menos correo o teléfono ----

    @Test
    fun se_exige_al_menos_un_medio_de_contacto() {
        assertNotNull(Validadores.contacto("", " "))
        assertNull(Validadores.contacto("a@example.cl", ""))
        assertNull(Validadores.contacto("", "912345678"))
    }

    @Test
    fun correo_y_telefono_opcionales_validan_el_formato_si_se_escriben() {
        assertNull(Validadores.correoOpcional("", "912345678"))
        assertNotNull(Validadores.correoOpcional("", ""))
        assertNotNull(Validadores.correoOpcional("malo", "912345678"))
        assertNull(Validadores.telefonoOpcional("", "a@example.cl"))
        assertNotNull(Validadores.telefonoOpcional("", ""))
        assertNotNull(Validadores.telefonoOpcional("123", "a@example.cl"))
        assertNull(Validadores.telefonoOpcional("9 1234 5678", ""))
    }
}
