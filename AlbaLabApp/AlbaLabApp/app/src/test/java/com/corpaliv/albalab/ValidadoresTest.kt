package com.corpaliv.albalab

import com.corpaliv.albalab.domain.model.TipoColaboracion
import com.corpaliv.albalab.domain.validation.Validadores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ValidadoresTest {

    @Test
    fun nombre_vacio_o_con_numeros_es_invalido() {
        assertNotNull(Validadores.nombre(""))
        assertNotNull(Validadores.nombre("Ana 123"))
        assertNotNull(Validadores.nombre("Al"))
    }

    @Test
    fun nombre_con_tildes_y_enie_es_valido() {
        assertNull(Validadores.nombre("María Ñuñoa Pérez"))
    }

    @Test
    fun correo_valido_e_invalido() {
        assertNull(Validadores.correo("persona.prueba@example.cl"))
        assertNotNull(Validadores.correo("persona@"))
        assertNotNull(Validadores.correo("sin-arroba.cl"))
        assertNotNull(Validadores.correo(""))
    }

    @Test
    fun telefono_es_opcional_pero_si_se_escribe_debe_ser_movil_chileno() {
        assertNull(Validadores.telefono(""))
        assertNull(Validadores.telefono("9 1234 5678"))
        assertNull(Validadores.telefono("+56912345678"))
        assertNotNull(Validadores.telefono("12345"))
        assertNotNull(Validadores.telefono("812345678"))
    }

    @Test
    fun clave_nueva_exige_largo_mayuscula_minuscula_y_numero() {
        assertNotNull(Validadores.claveNueva(""))
        assertNotNull(Validadores.claveNueva("Ab1"))
        assertNotNull(Validadores.claveNueva("abcdefgh1"))
        assertNotNull(Validadores.claveNueva("ABCDEFGH1"))
        assertNotNull(Validadores.claveNueva("Abcdefgh"))
        assertNull(Validadores.claveNueva("Abcdefg1"))
    }

    @Test
    fun confirmacion_debe_coincidir() {
        assertNull(Validadores.confirmarClave("Abcdefg1", "Abcdefg1"))
        assertEquals("Las contraseñas no coinciden", Validadores.confirmarClave("Abcdefg1", "Abcdefg2"))
        assertNotNull(Validadores.confirmarClave("Abcdefg1", ""))
    }

    @Test
    fun monto_solo_es_obligatorio_en_donaciones() {
        assertNotNull(Validadores.monto("", TipoColaboracion.DONACION))
        assertNull(Validadores.monto("", TipoColaboracion.VOLUNTARIADO))
        assertNull(Validadores.monto("abc", TipoColaboracion.VOLUNTARIADO))
    }

    @Test
    fun monto_respeta_minimo_y_maximo() {
        assertNotNull(Validadores.monto("500", TipoColaboracion.DONACION))
        assertNull(Validadores.monto("5000", TipoColaboracion.DONACION))
        assertNotNull(Validadores.monto("99999999", TipoColaboracion.DONACION))
    }

    @Test
    fun fecha_no_puede_ser_pasada_ni_mas_de_un_anio() {
        val hoy = LocalDate.of(2026, 10, 20)
        assertNotNull(Validadores.fechaContacto(null, hoy))
        assertNotNull(Validadores.fechaContacto(hoy.minusDays(1), hoy))
        assertNull(Validadores.fechaContacto(hoy, hoy))
        assertNull(Validadores.fechaContacto(hoy.plusMonths(3), hoy))
        assertNotNull(Validadores.fechaContacto(hoy.plusYears(1).plusDays(1), hoy))
    }

    @Test
    fun comuna_debe_estar_en_la_lista() {
        assertNull(Validadores.comuna("Ñuñoa"))
        assertNull(Validadores.comuna("providencia"))
        assertNotNull(Validadores.comuna("Atlantis"))
        assertNotNull(Validadores.comuna(""))
    }

    @Test
    fun organizacion_solo_se_exige_para_empresas() {
        assertNull(Validadores.organizacion("", requerida = false))
        assertNotNull(Validadores.organizacion("", requerida = true))
        assertNull(Validadores.organizacion("Empresa Ficticia SpA", requerida = true))
    }

    @Test
    fun terminos_deben_aceptarse() {
        assertNotNull(Validadores.terminos(false))
        assertNull(Validadores.terminos(true))
    }

    @Test
    fun formato_de_miles() {
        assertEquals("1.000", Validadores.formatoMiles(1000))
        assertEquals("10.000.000", Validadores.formatoMiles(10_000_000))
        assertEquals("999", Validadores.formatoMiles(999))
    }
}
