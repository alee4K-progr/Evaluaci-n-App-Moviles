package com.corpaliv.albalab

import com.corpaliv.albalab.data.remote.Resultado
import com.corpaliv.albalab.data.remote.TipoError
import com.corpaliv.albalab.data.remote.dto.EstadoPublicoDto
import com.corpaliv.albalab.data.repository.InteresRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class InteresRepositoryTest {

    private val dao = FakeRegistroDao()
    private val api = FakeVinculacionApi()
    private val preferencias = FakePreferencias(invitado = true)
    private val repo = InteresRepository(dao, api, preferencias)

    @Test
    fun conservaLaClaveDeIdempotenciaAlReintentar() = runBlocking {
        var clave: String? = null
        api.crear = { clave = it.claveIdempotencia; throw IOException("respuesta perdida") }
        val guardado = repo.guardarYEnviar(registroDePrueba())
        assertNotNull(clave)

        api.crear = { EstadoPublicoDto("AL-ABCD2345", "RECIBIDA") }
        assertEquals(listOf(guardado.id), repo.sincronizarPendientes())
        assertEquals(clave, api.enviados.single().claveIdempotencia)
    }

    @Test
    fun limitaLaLecturaElReenvioYElBorradoALaSesionActiva() = runBlocking {
        val cuenta = "ana@example.cl"
        val idCuenta = dao.insertar(registroDePrueba().copy(usuarioCorreo = cuenta))
        val idInvitado = dao.insertar(registroDePrueba())

        assertEquals(listOf(idInvitado), repo.registros.first { it.isNotEmpty() }.map { it.id })
        repo.eliminar(idCuenta)
        assertEquals(listOf(idInvitado), repo.sincronizarPendientes())

        preferencias.iniciarSesion(cuenta)
        assertEquals(listOf(idCuenta), repo.registros.first { it.isNotEmpty() }.map { it.id })
        assertEquals(listOf(idCuenta), repo.sincronizarPendientes())
    }

    @Test
    fun con_conexion_el_registro_se_guarda_y_queda_sincronizado_con_su_codigo() = runBlocking {
        val r = repo.guardarYEnviar(registroDePrueba())
        assertTrue(r.sincronizado)
        assertEquals(1, api.enviados.size)
        val guardado = dao.todos().single()
        assertTrue(guardado.sincronizado)
        assertEquals("AL-ABCD2345", guardado.remoteId)
        assertEquals("RECIBIDA", guardado.estadoRemoto)
    }

    @Test
    fun sin_conexion_el_registro_se_conserva_como_pendiente() = runBlocking {
        api.crear = { throw IOException("sin red") }
        val r = repo.guardarYEnviar(registroDePrueba())
        assertFalse(r.sincronizado)
        assertEquals(1, dao.todos().size)
        assertFalse(dao.todos().single().sincronizado)
        assertTrue(api.enviados.isEmpty())
    }

    @Test
    fun los_pendientes_se_envian_cuando_vuelve_la_conexion() = runBlocking {
        api.crear = { throw IOException("sin red") }
        repo.guardarYEnviar(registroDePrueba(correo = "a@example.cl", creadoEn = 1))
        repo.guardarYEnviar(registroDePrueba(correo = "b@example.cl", creadoEn = 2))

        api.crear = { EstadoPublicoDto("AL-1", "RECIBIDA", "x", "x") }
        val enviados = repo.sincronizarPendientes()

        assertEquals(2, enviados.size)
        assertTrue(dao.todos().all { it.sincronizado })
        assertEquals(listOf("a@example.cl", "b@example.cl"), api.enviados.map { it.email }) // orden de creación
    }

    @Test
    fun sin_conexion_no_se_intenta_con_los_demas_registros() = runBlocking {
        var intentos = 0
        api.crear = { intentos++; throw IOException("sin red") }
        dao.insertar(registroDePrueba(correo = "a@example.cl", creadoEn = 1))
        dao.insertar(registroDePrueba(correo = "b@example.cl", creadoEn = 2))

        assertTrue(repo.sincronizarPendientes().isEmpty())
        assertEquals(1, intentos)
    }

    @Test
    fun un_rechazo_del_servidor_no_bloquea_los_otros_registros() = runBlocking {
        api.crear = { dto ->
            if (dto.email == "malo@example.cl") throw httpError(400)
            EstadoPublicoDto("AL-1", "RECIBIDA", "x", "x")
        }
        dao.insertar(registroDePrueba(correo = "malo@example.cl", creadoEn = 1))
        dao.insertar(registroDePrueba(correo = "bueno@example.cl", creadoEn = 2))

        val enviados = repo.sincronizarPendientes()

        assertEquals(1, enviados.size)
        val porCorreo = dao.todos().associateBy { it.correo }
        assertFalse(porCorreo.getValue("malo@example.cl").sincronizado)
        assertTrue(porCorreo.getValue("bueno@example.cl").sincronizado)
    }

    @Test
    fun una_respuesta_sin_codigo_o_con_estado_invalido_deja_el_registro_pendiente() = runBlocking {
        api.crear = { EstadoPublicoDto(null, "RECIBIDA", "x", "x") }
        assertFalse(repo.guardarYEnviar(registroDePrueba()).sincronizado)
        api.crear = { throw IllegalStateException("fallo inesperado") }
        assertFalse(repo.guardarYEnviar(registroDePrueba()).sincronizado)
    }

    @Test
    fun un_registro_ya_sincronizado_no_se_vuelve_a_enviar() = runBlocking {
        repo.guardarYEnviar(registroDePrueba())
        repo.sincronizarPendientes()
        assertEquals(1, api.enviados.size)
    }

    @Test
    fun refrescarEstados_actualiza_solo_los_que_cambiaron_y_omite_estados_finales() = runBlocking {
        val a = dao.insertar(registroDePrueba(correo = "a@example.cl"))
        val b = dao.insertar(registroDePrueba(correo = "b@example.cl"))
        val c = dao.insertar(registroDePrueba(correo = "c@example.cl"))
        dao.marcarSincronizado(a, "AL-A", "RECIBIDA")
        dao.marcarSincronizado(b, "AL-B", "CONCRETADO") // final: no se consulta
        dao.marcarSincronizado(c, "AL-C", "RECIBIDA")
        val consultados = mutableListOf<String>()
        api.estado = { codigo ->
            consultados += codigo
            EstadoPublicoDto(codigo, if (codigo == "AL-A") "EN_REVISION" else "RECIBIDA", "x", "x")
        }

        assertEquals(1, repo.refrescarEstados())

        assertEquals(listOf("AL-A", "AL-C"), consultados)
        assertEquals("EN_REVISION", dao.buscar(a)!!.estadoRemoto)
        assertEquals("CONCRETADO", dao.buscar(b)!!.estadoRemoto)
    }

    @Test
    fun refrescarEstados_se_detiene_sin_conexion() = runBlocking {
        val a = dao.insertar(registroDePrueba(correo = "a@example.cl"))
        val b = dao.insertar(registroDePrueba(correo = "b@example.cl"))
        dao.marcarSincronizado(a, "AL-A", "RECIBIDA")
        dao.marcarSincronizado(b, "AL-B", "RECIBIDA")
        var intentos = 0
        api.estado = { intentos++; throw IOException("sin red") }
        assertEquals(0, repo.refrescarEstados())
        assertEquals(1, intentos)
    }

    @Test
    fun consultarEstado_valida_normaliza_y_clasifica_errores() = runBlocking {
        val vacio = repo.consultarEstado("  ") as Resultado.Fallo
        assertEquals(TipoError.INVALIDO, vacio.tipo)

        var codigoPedido = ""
        api.estado = { codigoPedido = it; EstadoPublicoDto(it, "CONTACTADO", "a", "b") }
        val ok = repo.consultarEstado(" al-abcd2345 ") as Resultado.Ok
        assertEquals("AL-ABCD2345", codigoPedido)
        assertEquals("CONTACTADO", ok.valor.estado.name)

        api.estado = { throw httpError(404, """{"error":"NO_ENCONTRADA","mensaje":"Solicitud no encontrada"}""") }
        val noExiste = repo.consultarEstado("AL-NADA2345") as Resultado.Fallo
        assertEquals(TipoError.NO_ENCONTRADO, noExiste.tipo)
        assertEquals("Solicitud no encontrada", noExiste.mensaje)

        api.estado = { EstadoPublicoDto(it, "INVENTADO", "a", "b") }
        assertEquals(TipoError.DESCONOCIDO, (repo.consultarEstado("AL-ABCD2345") as Resultado.Fallo).tipo)
    }

    @Test
    fun eliminar_borra_solo_el_registro_local() = runBlocking {
        repo.guardarYEnviar(registroDePrueba())
        repo.eliminar(dao.todos().single().id)
        assertTrue(dao.todos().isEmpty())
    }
}
