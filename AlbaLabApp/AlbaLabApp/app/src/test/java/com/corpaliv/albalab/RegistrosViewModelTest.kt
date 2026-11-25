package com.corpaliv.albalab

import com.corpaliv.albalab.data.remote.dto.EstadoPublicoDto
import com.corpaliv.albalab.data.repository.InteresRepository
import com.corpaliv.albalab.viewmodel.RegistrosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class RegistrosViewModelTest {

    private val dao = FakeRegistroDao()
    private val api = FakeVinculacionApi()

    @Before
    fun prepararMain() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun limpiarMain() = Dispatchers.resetMain()

    private fun vm() = RegistrosViewModel(InteresRepository(dao, api, FakePreferencias(invitado = true)))

    @Test
    fun al_abrir_envia_los_pendientes_y_refresca_los_estados() = runBlocking {
        dao.insertar(registroDePrueba(correo = "a@example.cl"))
        val vm = vm()
        assertEquals(1, api.enviados.size)
        assertTrue(dao.todos().single().sincronizado)
        assertFalse(vm.actualizando.value)
    }

    @Test
    fun sincronizar_actualiza_el_estado_de_un_registro_ya_enviado() = runBlocking {
        val id = dao.insertar(registroDePrueba())
        dao.marcarSincronizado(id, "AL-ABCD2345", "RECIBIDA")
        api.estado = { EstadoPublicoDto(it, "CONTACTADO", "x", "x") }
        vm()
        assertEquals("CONTACTADO", dao.buscar(id)!!.estadoRemoto)
    }

    @Test
    fun sin_conexion_al_abrir_no_falla_ni_pierde_registros() = runBlocking {
        api.crear = { throw IOException("sin red") }
        dao.insertar(registroDePrueba())
        val vm = vm()
        assertFalse(vm.actualizando.value)
        assertFalse(dao.todos().single().sincronizado)
    }

    @Test
    fun eliminar_borra_el_registro_local() = runBlocking {
        val id = dao.insertar(registroDePrueba())
        val vm = vm()
        vm.eliminar(id)
        assertTrue(dao.todos().isEmpty())
    }

    @Test
    fun el_codigo_se_normaliza_a_mayusculas_y_sin_caracteres_raros() {
        val vm = vm()
        vm.cambiarCodigo("al-ab cd#2345xyz")
        assertEquals("AL-ABCD2345", vm.consulta.value.codigo) // sin espacios ni símbolos y con máximo 11 caracteres
    }

    @Test
    fun consultar_con_formato_invalido_muestra_error_sin_llamar_al_servidor() {
        val vm = vm()
        var llamadas = 0
        api.estado = { llamadas++; EstadoPublicoDto(it, "RECIBIDA", "x", "x") }
        vm.cambiarCodigo("AL-1")
        vm.consultar()
        assertNotNull(vm.consulta.value.error)
        assertEquals(0, llamadas)
    }

    @Test
    fun consultar_con_codigo_valido_muestra_el_estado() {
        val vm = vm()
        api.estado = { EstadoPublicoDto(it, "EN_REVISION", "2026-03-10T12:00:00", "2026-03-11T09:30:00") }
        vm.cambiarCodigo("AL-ABCD2345")
        vm.consultar()
        val r = vm.consulta.value.resultado
        assertNotNull(r)
        assertEquals("En revisión", r!!.estado.etiqueta)
        assertNull(vm.consulta.value.error)
        assertFalse(vm.consulta.value.consultando)
    }

    @Test
    fun consultar_un_codigo_inexistente_muestra_el_mensaje_del_servidor() {
        val vm = vm()
        api.estado = { throw httpError(404, """{"error":"NO_ENCONTRADA","mensaje":"Solicitud no encontrada"}""") }
        vm.cambiarCodigo("AL-ABCD2345")
        vm.consultar()
        assertEquals("Solicitud no encontrada", vm.consulta.value.error)
        assertNull(vm.consulta.value.resultado)
    }

    @Test
    fun escribir_de_nuevo_limpia_el_resultado_anterior() {
        val vm = vm()
        vm.cambiarCodigo("AL-ABCD2345")
        vm.consultar()
        assertNotNull(vm.consulta.value.resultado)
        vm.cambiarCodigo("AL-ABCD234")
        assertNull(vm.consulta.value.resultado)
    }
}
