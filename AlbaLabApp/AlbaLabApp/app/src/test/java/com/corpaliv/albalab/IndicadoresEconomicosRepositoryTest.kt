package com.corpaliv.albalab

import com.corpaliv.albalab.data.remote.IndicadoresApi
import com.corpaliv.albalab.data.remote.RedFactory
import com.corpaliv.albalab.data.repository.IndicadoresEconomicosRepository
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Usa respuestas simuladas con la estructura documentada públicamente de mindicador.cl
 * (objeto raíz con `dolar` y `uf`, cada uno con `valor` y `fecha`).
 */
class IndicadoresEconomicosRepositoryTest {

    private lateinit var servidor: MockWebServer
    private lateinit var repo: IndicadoresEconomicosRepository

    @Before
    fun preparar() {
        servidor = MockWebServer().also { it.start() }
        repo = IndicadoresEconomicosRepository(
            RedFactory.crear(servidor.url("/").toString(), IndicadoresApi::class.java)
        )
    }

    @After
    fun cerrar() {
        runCatching { servidor.shutdown() } // un test puede haberlo cerrado antes
    }

    private fun responder(cuerpo: String, codigo: Int = 200) = servidor.enqueue(
        MockResponse().setResponseCode(codigo).setHeader("Content-Type", "application/json").setBody(cuerpo)
    )

    @Test
    fun lee_el_dolar_observado() = runBlocking {
        responder(
            """{"uf":{"codigo":"uf","valor":39000.5,"fecha":"2026-11-18T03:00:00.000Z"},
                "dolar":{"codigo":"dolar","valor":945.5,"fecha":"2026-11-18T03:00:00.000Z"}}"""
        )
        val cambio = repo.dolarObservado()
        assertNotNull(cambio)
        assertEquals(945.5, cambio!!.clpPorDolar, 0.0001)
        assertEquals("/api", servidor.takeRequest().path)
    }

    @Test
    fun respuesta_sin_dolar_devuelve_null() = runBlocking {
        responder("""{"uf":{"valor":39000.5}}""")
        assertNull(repo.dolarObservado())
    }

    @Test
    fun valor_cero_o_negativo_devuelve_null() = runBlocking {
        responder("""{"dolar":{"valor":0}}""")
        assertNull(repo.dolarObservado())
    }

    @Test
    fun error_http_o_json_invalido_devuelve_null_sin_lanzar_excepcion() = runBlocking {
        responder("{}", codigo = 503)
        assertNull(repo.dolarObservado())
        responder("no es json")
        assertNull(repo.dolarObservado())
    }
}
