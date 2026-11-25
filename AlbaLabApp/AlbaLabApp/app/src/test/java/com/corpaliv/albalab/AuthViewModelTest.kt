package com.corpaliv.albalab

import com.corpaliv.albalab.data.repository.UsuarioRepository
import com.corpaliv.albalab.viewmodel.AuthViewModel
import com.corpaliv.albalab.viewmodel.CampoLogin
import com.corpaliv.albalab.viewmodel.CampoRegistro
import com.corpaliv.albalab.viewmodel.EstadoSesion
import com.corpaliv.albalab.viewmodel.EventoUi
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

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dao = FakeUsuarioDao()
    private val preferencias = FakePreferencias()

    @Before
    fun prepararMain() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun limpiarMain() = Dispatchers.resetMain()

    private fun vm() = AuthViewModel(UsuarioRepository(dao), preferencias)

    /**
     * El repositorio calcula el hash en Dispatchers.Default (otro hilo), así que se espera a que el
     * resultado llegue al estado en vez de asumir que ya terminó.
     */
    private fun esperar(descripcion: String, condicion: () -> Boolean) {
        val limite = System.currentTimeMillis() + 10_000
        while (!condicion()) {
            if (System.currentTimeMillis() > limite) throw AssertionError("Tiempo agotado esperando: $descripcion")
            Thread.sleep(10)
        }
    }

    private fun AuthViewModel.completarRegistro(clave: String = "Clave1234", terminos: Boolean = true) {
        registroNombre("Ana Prueba")
        registroCorreo("ana@example.cl")
        registroClave(clave)
        registroConfirmacion(clave)
        registroTerminos(terminos)
    }

    @Test
    fun al_inicio_sin_datos_la_sesion_es_sin_sesion() {
        assertEquals(EstadoSesion.SinSesion, vm().sesion.value)
    }

    @Test
    fun crear_cuenta_inicia_sesion_y_guarda_el_usuario() = runBlocking {
        val vm = vm()
        vm.completarRegistro()
        vm.crearCuenta()
        esperar("sesión autenticada") { vm.sesion.value is EstadoSesion.Autenticado }
        val s = vm.sesion.value
        assertTrue(s is EstadoSesion.Autenticado)
        assertEquals("ana@example.cl", (s as EstadoSesion.Autenticado).usuario.correo)
        assertFalse(s.usuario.claveHash.contains("Clave1234")) // nunca se guarda en texto plano
        assertEquals(EventoUi.VibrarExito, vm.eventos.first())
    }

    @Test
    fun registro_invalido_no_crea_cuenta() = runBlocking {
        val vm = vm()
        vm.completarRegistro(terminos = false)
        vm.crearCuenta()
        assertTrue(vm.registro.value.intentoEnvio)
        assertNotNull(vm.registro.value.error(CampoRegistro.TERMINOS))
        assertEquals(EstadoSesion.SinSesion, vm.sesion.value)
        assertEquals(EventoUi.VibrarError, vm.eventos.first())
    }

    @Test
    fun correo_ya_registrado_muestra_error_general() {
        val vm = vm()
        vm.completarRegistro()
        vm.crearCuenta()
        esperar("primera cuenta creada") { vm.sesion.value is EstadoSesion.Autenticado }
        vm.cerrarSesion()
        esperar("sesión cerrada") { vm.sesion.value == EstadoSesion.SinSesion }
        vm.limpiarRegistro()
        vm.completarRegistro()
        vm.crearCuenta()
        esperar("error de correo repetido") { vm.registro.value.errorGeneral != null }
        assertEquals("Ya existe una cuenta con ese correo", vm.registro.value.errorGeneral)
        assertFalse(vm.registro.value.cargando)
    }

    @Test
    fun login_correcto_e_incorrecto() = runBlocking {
        val vm = vm()
        vm.completarRegistro()
        vm.crearCuenta()
        esperar("cuenta creada") { vm.sesion.value is EstadoSesion.Autenticado }
        vm.cerrarSesion()
        esperar("sesión cerrada") { vm.sesion.value == EstadoSesion.SinSesion }

        vm.loginCorreo("ana@example.cl")
        vm.loginClave("Equivocada1")
        vm.ingresar()
        esperar("error de credenciales") { vm.login.value.errorGeneral != null }
        assertEquals("Correo o contraseña incorrectos", vm.login.value.errorGeneral)
        assertEquals(EstadoSesion.SinSesion, vm.sesion.value)

        vm.loginClave("Clave1234")
        vm.ingresar()
        esperar("ingreso correcto") { vm.sesion.value is EstadoSesion.Autenticado }
        assertTrue(vm.sesion.value is EstadoSesion.Autenticado)
        assertEquals("", vm.login.value.clave) // el formulario se limpia
    }

    @Test
    fun login_con_campos_vacios_no_consulta() {
        val vm = vm()
        vm.ingresar()
        assertTrue(vm.login.value.intentoEnvio)
        assertNotNull(vm.login.value.error(CampoLogin.CORREO))
        assertNotNull(vm.login.value.error(CampoLogin.CLAVE))
        assertEquals(EstadoSesion.SinSesion, vm.sesion.value)
    }

    @Test
    fun invitado_y_cerrar_sesion() {
        val vm = vm()
        vm.continuarComoInvitado()
        assertEquals(EstadoSesion.Invitado, vm.sesion.value)
        vm.cerrarSesion()
        assertEquals(EstadoSesion.SinSesion, vm.sesion.value)
    }

    @Test
    fun alternar_visibilidad_y_tocar_campos() {
        val vm = vm()
        vm.loginAlternarClave()
        vm.registroAlternarClave()
        vm.loginTocar(CampoLogin.CLAVE)
        vm.registroTocar(CampoRegistro.NOMBRE)
        assertTrue(vm.login.value.mostrarClave)
        assertTrue(vm.registro.value.mostrarClave)
        assertTrue(vm.login.value.mostrarError(CampoLogin.CLAVE))
        assertTrue(vm.registro.value.mostrarError(CampoRegistro.NOMBRE))
        assertFalse(vm.registro.value.mostrarError(CampoRegistro.CORREO))
        vm.registroNombre("")
        vm.limpiarRegistro()
        assertEquals("", vm.registro.value.nombre)
        assertNull(vm.registro.value.errorGeneral)
    }
}
