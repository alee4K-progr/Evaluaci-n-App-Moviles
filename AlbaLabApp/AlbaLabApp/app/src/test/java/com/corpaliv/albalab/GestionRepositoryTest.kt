package com.corpaliv.albalab

import com.corpaliv.albalab.data.remote.Resultado
import com.corpaliv.albalab.data.remote.TipoError
import com.corpaliv.albalab.data.remote.dto.HistorialDto
import com.corpaliv.albalab.data.repository.AlmacenSesionGestor
import com.corpaliv.albalab.data.repository.GestionRepository
import com.corpaliv.albalab.domain.model.EstadoSolicitud
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class GestionRepositoryTest {

    private val auth = FakeAuthApi()
    private val api = FakeVinculacionApi()
    private val almacen = AlmacenSesionGestor()
    private val repo = GestionRepository(auth, api, almacen)

    private suspend fun iniciarSesion() = repo.login("gestor@example.cl", "Gestor123")

    @Test
    fun login_correcto_guarda_la_sesion_en_memoria() = runBlocking {
        val r = iniciarSesion() as Resultado.Ok
        assertEquals("GESTOR", r.valor.rol)
        assertEquals("token-de-prueba", almacen.token)
        assertNotNull(repo.sesion.value)
    }

    @Test
    fun login_con_credenciales_malas_da_mensaje_claro_y_no_guarda_sesion() = runBlocking {
        auth.login = { throw httpError(401, """{"error":"CREDENCIALES_INVALIDAS","mensaje":"x"}""") }
        val r = iniciarSesion() as Resultado.Fallo
        assertEquals(TipoError.NO_AUTORIZADO, r.tipo)
        assertEquals("Correo o contraseña incorrectos", r.mensaje)
        assertNull(almacen.token)
    }

    @Test
    fun login_sin_red_informa_sin_conexion() = runBlocking {
        auth.login = { throw IOException("x") }
        assertEquals(TipoError.SIN_CONEXION, (iniciarSesion() as Resultado.Fallo).tipo)
    }

    @Test
    fun login_con_respuesta_sin_token_o_rol_es_error() = runBlocking {
        auth.login = { com.corpaliv.albalab.data.remote.dto.LoginResponseDto(token = null, rol = "GESTOR") }
        assertEquals(TipoError.DESCONOCIDO, (iniciarSesion() as Resultado.Fallo).tipo)
        auth.login = { com.corpaliv.albalab.data.remote.dto.LoginResponseDto(token = "t", rol = " ") }
        assertEquals(TipoError.DESCONOCIDO, (iniciarSesion() as Resultado.Fallo).tipo)
        assertNull(almacen.token)
    }

    @Test
    fun cerrar_sesion_borra_el_token() = runBlocking {
        iniciarSesion()
        repo.cerrarSesion()
        assertNull(almacen.token)
    }

    @Test
    fun listar_filtra_por_estado_y_descarta_elementos_invalidos() = runBlocking {
        var estadoPedido: String? = "sin-llamar"
        api.listar = { e ->
            estadoPedido = e
            listOf(solicitudDtoDePrueba(1), solicitudDtoDePrueba(2).copy(estado = "ROTO"))
        }
        val r = repo.listar(EstadoSolicitud.RECIBIDA) as Resultado.Ok
        assertEquals("RECIBIDA", estadoPedido)
        assertEquals(listOf(1L), r.valor.map { it.id })
        repo.listar(null)
        assertNull(estadoPedido)
    }

    @Test
    fun un_401_cierra_la_sesion_local() = runBlocking {
        iniciarSesion()
        api.listar = { throw httpError(401) }
        val r = repo.listar(null) as Resultado.Fallo
        assertEquals(TipoError.NO_AUTORIZADO, r.tipo)
        assertNull(almacen.token)
    }

    @Test
    fun un_403_o_un_error_de_red_no_cierran_la_sesion() = runBlocking {
        iniciarSesion()
        api.listar = { throw httpError(403) }
        repo.listar(null)
        api.listar = { throw IOException("x") }
        repo.listar(null)
        assertNotNull(almacen.token)
    }

    @Test
    fun detalle_e_historial() = runBlocking {
        api.historial = { listOf(HistorialDto(1L, null, "RECIBIDA", "ingreso", "app", "2026-03-10T12:00:00")) }
        assertEquals("AL-ABCD2345", (repo.detalle(1) as Resultado.Ok).valor.codigo)
        assertEquals(1, (repo.historial(1) as Resultado.Ok).valor.size)
        api.detalle = { solicitudDtoDePrueba(it).copy(estado = "XX") }
        assertEquals(TipoError.DESCONOCIDO, (repo.detalle(1) as Resultado.Fallo).tipo)
        api.detalle = { throw httpError(404) }
        assertEquals(TipoError.NO_ENCONTRADO, (repo.detalle(1) as Resultado.Fallo).tipo)
    }

    @Test
    fun cambiar_estado_envia_estado_y_observacion_recortada() = runBlocking {
        var enviado: com.corpaliv.albalab.data.remote.dto.CambiarEstadoRequestDto? = null
        api.cambiar = { id, c -> enviado = c; solicitudDtoDePrueba(id).copy(estado = c.estado) }
        val r = repo.cambiarEstado(1, EstadoSolicitud.EN_REVISION, "  revisando  ") as Resultado.Ok
        assertEquals(EstadoSolicitud.EN_REVISION, r.valor.estado)
        assertEquals("EN_REVISION", enviado!!.estado)
        assertEquals("revisando", enviado!!.observacion)
        repo.cambiarEstado(1, EstadoSolicitud.EN_REVISION, "   ")
        assertNull(enviado!!.observacion)
    }

    @Test
    fun cambiar_estado_con_transicion_invalida_devuelve_conflicto() = runBlocking {
        api.cambiar = { _, _ -> throw httpError(409, """{"error":"TRANSICION_INVALIDA","mensaje":"No se puede pasar de RECIBIDA a CONCRETADO"}""") }
        val r = repo.cambiarEstado(1, EstadoSolicitud.CONCRETADO, null) as Resultado.Fallo
        assertEquals(TipoError.CONFLICTO, r.tipo)
        assertTrue(r.mensaje.contains("RECIBIDA"))
    }

    @Test
    fun eliminar_llama_al_servidor() = runBlocking {
        assertTrue(repo.eliminar(5) is Resultado.Ok)
        assertEquals(listOf(5L), api.eliminados)
        api.eliminar = { throw httpError(404) }
        assertEquals(TipoError.NO_ENCONTRADO, (repo.eliminar(9) as Resultado.Fallo).tipo)
    }
}
