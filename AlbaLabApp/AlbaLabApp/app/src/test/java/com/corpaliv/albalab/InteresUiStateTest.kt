package com.corpaliv.albalab

import com.corpaliv.albalab.domain.model.TipoCambio
import com.corpaliv.albalab.domain.model.TipoColaboracion
import com.corpaliv.albalab.viewmodel.CampoInteres
import com.corpaliv.albalab.viewmodel.InteresUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InteresUiStateTest {

    private val cambio = TipoCambio(clpPorDolar = 1_000.0)

    @Test
    fun donacion_con_monto_valido_calcula_equivalente_en_dolares() {
        val e = InteresUiState(tipo = TipoColaboracion.DONACION, monto = "50000", tipoCambio = cambio)
        assertEquals(50.0, e.montoEnDolares!!, 0.0001)
    }

    @Test
    fun sin_tipo_de_cambio_no_hay_equivalente() {
        val e = InteresUiState(tipo = TipoColaboracion.DONACION, monto = "50000")
        assertNull(e.montoEnDolares)
    }

    @Test
    fun monto_invalido_o_tipo_sin_monto_no_calcula() {
        assertNull(InteresUiState(tipo = TipoColaboracion.DONACION, monto = "10", tipoCambio = cambio).montoEnDolares)
        assertNull(InteresUiState(tipo = TipoColaboracion.DONACION, monto = "", tipoCambio = cambio).montoEnDolares)
        assertNull(InteresUiState(tipo = TipoColaboracion.VOLUNTARIADO, monto = "50000", tipoCambio = cambio).montoEnDolares)
    }

    @Test
    fun basta_un_medio_de_contacto() {
        assertNull(InteresUiState(correo = "ana@example.cl").error(CampoInteres.CORREO))
        assertNull(InteresUiState(correo = "ana@example.cl").error(CampoInteres.TELEFONO))
        assertNull(InteresUiState(telefono = "912345678").error(CampoInteres.CORREO))
        assertNotNull(InteresUiState().error(CampoInteres.CORREO))
        assertNotNull(InteresUiState().error(CampoInteres.TELEFONO))
    }

    @Test
    fun los_errores_solo_se_muestran_tras_tocar_el_campo_o_intentar_enviar() {
        val limpio = InteresUiState()
        assertFalse(limpio.mostrarError(CampoInteres.NOMBRE))
        assertTrue(limpio.copy(tocados = setOf(CampoInteres.NOMBRE)).mostrarError(CampoInteres.NOMBRE))
        assertTrue(limpio.copy(intentoEnvio = true).mostrarError(CampoInteres.MENSAJE))
    }
}
