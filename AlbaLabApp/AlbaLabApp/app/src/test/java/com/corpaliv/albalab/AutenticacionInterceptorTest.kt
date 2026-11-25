package com.corpaliv.albalab

import com.corpaliv.albalab.data.remote.AutenticacionInterceptor
import com.corpaliv.albalab.data.remote.RedFactory
import com.corpaliv.albalab.data.remote.VinculacionApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** Verifica con un servidor simulado que el JWT viaja solo en las llamadas de gestión. */
class AutenticacionInterceptorTest {

    private lateinit var servidor: MockWebServer
    private var token: String? = "jwt-de-prueba"
    private lateinit var api: VinculacionApi

    @Before
    fun preparar() {
        servidor = MockWebServer().also { it.start() }
        val cliente = RedFactory.construirCliente(AutenticacionInterceptor { token })
        api = RedFactory.crear(servidor.url("/").toString(), VinculacionApi::class.java, cliente)
    }

    @After
    fun cerrar() {
        runCatching { servidor.shutdown() }
    }

    private fun json(cuerpo: String) = servidor.enqueue(
        MockResponse().setHeader("Content-Type", "application/json").setBody(cuerpo)
    )

    @Test
    fun las_llamadas_de_gestion_llevan_el_token_y_no_la_cabecera_interna() = runBlocking {
        json("[]")
        api.listar("RECIBIDA")
        val pedido = servidor.takeRequest()
        assertEquals("Bearer jwt-de-prueba", pedido.getHeader("Authorization"))
        assertNull(pedido.getHeader("X-Requiere-Sesion"))
        assertEquals("/vinculacion/solicitudes?estado=RECIBIDA", pedido.path)
    }

    @Test
    fun las_llamadas_publicas_nunca_llevan_token() = runBlocking {
        json("""{"codigo":"AL-ABCD2345","estado":"RECIBIDA"}""")
        api.estado("AL-ABCD2345")
        assertNull(servidor.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun sin_sesion_la_llamada_de_gestion_sale_sin_token() = runBlocking {
        token = null
        json("[]")
        api.listar(null)
        assertNull(servidor.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun cambiar_estado_usa_patch_con_el_cuerpo_esperado() = runBlocking {
        json("""{"id":1,"codigo":"AL-ABCD2345","nombreContacto":"Ana","estado":"EN_REVISION"}""")
        api.cambiarEstado(1L, com.corpaliv.albalab.data.remote.dto.CambiarEstadoRequestDto("EN_REVISION", "ok"))
        val pedido = servidor.takeRequest()
        assertEquals("PATCH", pedido.method)
        assertEquals("/vinculacion/solicitudes/1/estado", pedido.path)
        assertEquals("""{"estado":"EN_REVISION","observacion":"ok"}""", pedido.body.readUtf8())
    }
}
