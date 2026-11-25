package com.corpaliv.albalab

import com.corpaliv.albalab.data.remote.Resultado
import com.corpaliv.albalab.data.remote.TipoError
import com.corpaliv.albalab.data.remote.llamarApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class ResultadoTest {

    private fun tipoDe(r: Resultado<*>) = (r as Resultado.Fallo).tipo

    @Test
    fun exito_devuelve_el_valor() = runBlocking {
        assertEquals(7, (llamarApi { 7 } as Resultado.Ok).valor)
    }

    @Test
    fun sin_red_es_sin_conexion() = runBlocking {
        assertEquals(TipoError.SIN_CONEXION, tipoDe(llamarApi { throw IOException("x") }))
    }

    @Test
    fun codigos_http_se_clasifican() = runBlocking {
        val esperado = mapOf(
            400 to TipoError.INVALIDO, 401 to TipoError.NO_AUTORIZADO, 403 to TipoError.SIN_PERMISO,
            404 to TipoError.NO_ENCONTRADO, 409 to TipoError.CONFLICTO, 500 to TipoError.SERVIDOR,
            503 to TipoError.SERVIDOR, 418 to TipoError.DESCONOCIDO
        )
        for ((codigo, tipo) in esperado) {
            assertEquals("HTTP $codigo", tipo, tipoDe(llamarApi { throw httpError(codigo) }))
        }
    }

    @Test
    fun usa_el_mensaje_del_servidor_si_viene_y_uno_por_defecto_si_no() = runBlocking {
        val conMensaje = llamarApi { throw httpError(409, """{"error":"TRANSICION_INVALIDA","mensaje":"No se puede pasar de A a B"}""") }
        assertEquals("No se puede pasar de A a B", (conMensaje as Resultado.Fallo).mensaje)
        val sinCuerpo = llamarApi { throw httpError(409) } as Resultado.Fallo
        assertTrue(sinCuerpo.mensaje.isNotBlank())
        val cuerpoRaro = llamarApi { throw httpError(400, "<html>no es json</html>") } as Resultado.Fallo
        assertTrue(cuerpoRaro.mensaje.isNotBlank())
    }

    @Test
    fun excepcion_inesperada_es_desconocida() = runBlocking {
        assertEquals(TipoError.DESCONOCIDO, tipoDe(llamarApi { throw IllegalStateException("x") }))
    }

    @Test
    fun la_cancelacion_se_relanza() = runBlocking {
        try {
            llamarApi { throw CancellationException("cancelada") }
            fail("debía relanzar la cancelación")
        } catch (e: CancellationException) {
            // esperado
        }
    }
}
