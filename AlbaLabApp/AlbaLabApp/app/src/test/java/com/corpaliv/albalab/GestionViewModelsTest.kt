package com.corpaliv.albalab

import com.corpaliv.albalab.data.remote.dto.HistorialDto
import com.corpaliv.albalab.data.repository.AlmacenSesionGestor
import com.corpaliv.albalab.data.repository.GestionRepository
import com.corpaliv.albalab.domain.model.EstadoSolicitud
import com.corpaliv.albalab.viewmodel.EventoUi
import com.corpaliv.albalab.viewmodel.GestionViewModel
import com.corpaliv.albalab.viewmodel.GestorLoginViewModel
import com.corpaliv.albalab.viewmodel.SolicitudDetalleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class GestionViewModelsTest {

    private val auth = FakeAuthApi()
    private val api = FakeVinculacionApi()
    private val almacen = AlmacenSesionGestor()
    private val repo = GestionRepository(auth, api, almacen)

    @Before
    fun prepararMain() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun limpiarMain() = Dispatchers.resetMain()

    // ---- Login ----

    @Test
    fun login_con_campos_invalidos_no_llama_al_servidor() {
        var llamadas = 0
        auth.login = { llamadas++; throw IOException("no debía llamarse") }
        val vm = GestorLoginViewModel(repo)
        vm.cambiarCorreo("no-es-correo")
        vm.ingresar()
        assertTrue(vm.estado.value.intentoEnvio)
        assertFalse(vm.estado.value.esValido)
        assertEquals(0, llamadas)
    }

    @Test
    fun login_correcto_marca_ingreso_exitoso_y_borra_la_clave() {
        val vm = GestorLoginViewModel(repo)
        vm.cambiarCorreo("gestor@example.cl")
        vm.cambiarClave("Gestor123")
        vm.ingresar()
        assertTrue(vm.estado.value.ingresoExitoso)
        assertEquals("", vm.estado.value.clave)
        assertNotNull(almacen.token)
    }

    @Test
    fun login_fallido_muestra_error_y_se_limpia_al_escribir() {
        auth.login = { throw httpError(401) }
        val vm = GestorLoginViewModel(repo)
        vm.cambiarCorreo("gestor@example.cl")
        vm.cambiarClave("mala")
        vm.ingresar()
        assertEquals("Correo o contraseña incorrectos", vm.estado.value.error)
        assertFalse(vm.estado.value.ingresoExitoso)
        vm.cambiarClave("otra")
        assertNull(vm.estado.value.error)
    }

    @Test
    fun alternar_clave_cambia_la_visibilidad() {
        val vm = GestorLoginViewModel(repo)
        vm.alternarClave()
        assertTrue(vm.estado.value.mostrarClave)
    }

    // ---- Lista ----

    private fun sesionIniciada() = runBlocking { repo.login("gestor@example.cl", "Gestor123") }

    @Test
    fun la_lista_se_carga_y_se_filtra_por_estado() {
        sesionIniciada()
        api.listar = { e ->
            listOf(solicitudDtoDePrueba(1), solicitudDtoDePrueba(2).copy(estado = "EN_REVISION"))
                .filter { e == null || it.estado == e }
        }
        val vm = GestionViewModel(repo)
        vm.cargar()
        assertEquals(2, vm.estado.value.solicitudes.size)
        vm.cambiarFiltro(EstadoSolicitud.EN_REVISION)
        assertEquals(listOf(2L), vm.estado.value.solicitudes.map { it.id })
        assertEquals(EstadoSolicitud.EN_REVISION, vm.estado.value.filtro)
        assertFalse(vm.estado.value.cargando)
    }

    @Test
    fun error_de_red_en_la_lista_muestra_mensaje() {
        sesionIniciada()
        api.listar = { throw IOException("x") }
        val vm = GestionViewModel(repo)
        vm.cargar()
        assertNotNull(vm.estado.value.error)
        assertFalse(vm.estado.value.cargando)
    }

    @Test
    fun token_vencido_cierra_la_sesion() {
        sesionIniciada()
        api.listar = { throw httpError(401) }
        val vm = GestionViewModel(repo)
        assertNotNull(vm.sesion.value)
        vm.cargar()
        assertNull(vm.sesion.value)
    }

    @Test
    fun cerrar_sesion_limpia_el_token() {
        sesionIniciada()
        val vm = GestionViewModel(repo)
        vm.cerrarSesion()
        assertNull(vm.sesion.value)
    }

    // ---- Detalle ----

    private fun detalle(): SolicitudDetalleViewModel {
        sesionIniciada()
        val vm = SolicitudDetalleViewModel(repo)
        vm.cargar(1L)
        return vm
    }

    @Test
    fun el_detalle_carga_la_solicitud_y_su_historial() {
        api.historial = { listOf(HistorialDto(1L, null, "RECIBIDA", "ingreso", "app", "2026-03-10T12:00:00")) }
        val vm = detalle()
        assertEquals("AL-ABCD2345", vm.estado.value.solicitud!!.codigo)
        assertEquals(1, vm.estado.value.historial.size)
        assertFalse(vm.estado.value.cargando)
    }

    @Test
    fun detalle_inexistente_muestra_error() {
        api.detalle = { throw httpError(404) }
        val vm = detalle()
        assertNull(vm.estado.value.solicitud)
        assertNotNull(vm.estado.value.error)
    }

    @Test
    fun descartar_sin_observacion_devuelve_error_y_no_llama() {
        var llamadas = 0
        api.cambiar = { id, c -> llamadas++; solicitudDtoDePrueba(id).copy(estado = c.estado) }
        val vm = detalle()
        assertNotNull(vm.cambiarEstado(EstadoSolicitud.DESCARTADO, " "))
        assertNotNull(vm.cambiarEstado(null, ""))
        assertEquals(0, llamadas)
    }

    @Test
    fun cambio_de_estado_correcto_actualiza_la_solicitud_y_avisa() = runBlocking {
        val vm = detalle()
        assertNull(vm.cambiarEstado(EstadoSolicitud.EN_REVISION, "revisando"))
        assertEquals(EstadoSolicitud.EN_REVISION, vm.estado.value.solicitud!!.estado)
        assertFalse(vm.estado.value.procesando)
        assertEquals(EventoUi.VibrarExito, vm.eventos.first())
    }

    @Test
    fun cambio_de_estado_rechazado_por_el_servidor_avisa_y_conserva_el_estado() = runBlocking {
        api.cambiar = { _, _ -> throw httpError(409, """{"error":"TRANSICION_INVALIDA","mensaje":"No permitido"}""") }
        val vm = detalle()
        assertNull(vm.cambiarEstado(EstadoSolicitud.CONTACTADO, ""))
        assertEquals(EstadoSolicitud.RECIBIDA, vm.estado.value.solicitud!!.estado)
        assertEquals(EventoUi.VibrarError, vm.eventos.first())
    }

    @Test
    fun eliminar_marca_la_solicitud_como_eliminada() {
        val vm = detalle()
        vm.eliminar()
        assertTrue(vm.estado.value.eliminada)
        assertEquals(listOf(1L), api.eliminados)
    }

    @Test
    fun eliminar_con_error_no_marca_eliminada() = runBlocking {
        api.eliminar = { throw httpError(403) }
        val vm = detalle()
        vm.eliminar()
        assertFalse(vm.estado.value.eliminada)
        assertTrue(vm.eventos.first() is EventoUi.Mensaje)
    }

    @Test
    fun sin_cargar_no_se_puede_cambiar_ni_eliminar() {
        val vm = SolicitudDetalleViewModel(repo)
        assertEquals("No hay solicitud cargada", vm.cambiarEstado(EstadoSolicitud.EN_REVISION, ""))
        vm.eliminar()
        assertTrue(api.eliminados.isEmpty())
    }
}
