package com.corpaliv.albalab

import com.corpaliv.albalab.data.remote.IndicadoresApi
import com.corpaliv.albalab.data.remote.dto.EstadoPublicoDto
import com.corpaliv.albalab.data.remote.dto.MindicadorDto
import com.corpaliv.albalab.data.remote.dto.SerieIndicadorDto
import com.corpaliv.albalab.data.repository.IndicadoresEconomicosRepository
import com.corpaliv.albalab.data.repository.InteresRepository
import com.corpaliv.albalab.domain.model.TipoColaboracion
import com.corpaliv.albalab.viewmodel.CampoInteres
import com.corpaliv.albalab.viewmodel.EventoUi
import com.corpaliv.albalab.viewmodel.InteresViewModel
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class InteresViewModelTest {

    private val dao = FakeRegistroDao()
    private val api = FakeVinculacionApi()
    private val preferencias = FakePreferencias()
    private var dolar: Double? = 1000.0
    private val indicadores = IndicadoresEconomicosRepository(object : IndicadoresApi {
        override suspend fun indicadoresDelDia(): MindicadorDto =
            dolar?.let { MindicadorDto(dolar = SerieIndicadorDto(valor = it, fecha = "2026-03-10")) }
                ?: throw IOException("sin red")
    })

    @Before
    fun prepararMain() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun limpiarMain() = Dispatchers.resetMain()

    private fun vm() = InteresViewModel(InteresRepository(dao, api, preferencias), preferencias, indicadores)

    /** Completa los campos obligatorios con datos ficticios; solo teléfono o solo correo según se pida. */
    private fun InteresViewModel.llenar(correo: String = "ana@example.cl", telefono: String = "") {
        cambiarNombre("Ana Prueba")
        cambiarCorreo(correo)
        cambiarTelefono(telefono)
        cambiarComuna("Providencia")
        cambiarTipo(TipoColaboracion.VOLUNTARIADO)
        cambiarFecha(LocalDate.now().plusDays(3))
        cambiarTerminos(true)
    }

    @Test
    fun formulario_incompleto_no_se_envia() {
        val vm = vm()
        vm.enviar(null)
        assertTrue(vm.estado.value.intentoEnvio)
        assertFalse(vm.estado.value.esValido)
        assertTrue(api.enviados.isEmpty())
        assertTrue(dao.todos().isEmpty())
    }

    @Test
    fun solo_con_telefono_el_formulario_es_valido() {
        val vm = vm()
        vm.llenar(correo = "", telefono = "9 1234 5678")
        assertTrue(vm.estado.value.esValido)
        assertNull(vm.estado.value.error(CampoInteres.CORREO))
    }

    @Test
    fun sin_correo_ni_telefono_ambos_campos_muestran_error() {
        val vm = vm()
        vm.llenar(correo = "", telefono = "")
        assertNotNull(vm.estado.value.error(CampoInteres.CORREO))
        assertNotNull(vm.estado.value.error(CampoInteres.TELEFONO))
        assertTrue(vm.estado.value.mostrarError(CampoInteres.TELEFONO)) // al editar uno se evalúa el otro
    }

    @Test
    fun telefono_filtra_caracteres_y_limita_el_largo() {
        val vm = vm()
        vm.cambiarTelefono("9a1b2-3(4)5 6789012345")
        assertEquals("912345 67890123", vm.estado.value.telefono)
    }

    @Test
    fun envio_exitoso_guarda_envia_y_vibra() = runBlocking {
        val vm = vm()
        vm.llenar()
        vm.enviar("ana@example.cl")
        val e = vm.estado.value
        assertTrue(e.guardadoExitoso)
        assertFalse(e.guardando)
        assertEquals(1, api.enviados.size)
        assertEquals("ana@example.cl", dao.todos().single().usuarioCorreo)
        assertEquals("AL-ABCD2345", dao.todos().single().remoteId)
        assertEquals(EventoUi.VibrarExito, vm.eventos.first())
    }

    @Test
    fun sin_conexion_guarda_local_y_avisa_que_se_enviara_despues() = runBlocking {
        api.crear = { throw IOException("sin red") }
        val vm = vm()
        vm.llenar()
        vm.enviar(null)
        assertTrue(vm.estado.value.guardadoExitoso)
        assertFalse(dao.todos().single().sincronizado)
        assertEquals(EventoUi.VibrarExito, vm.eventos.first())
        val aviso = vm.eventos.first()
        assertTrue(aviso is EventoUi.Mensaje && aviso.texto.contains("conexión"))
    }

    @Test
    fun con_notificaciones_activas_se_pide_notificar_el_registro() = runBlocking {
        val vm = vm()
        vm.llenar()
        vm.enviar(null)
        assertEquals(EventoUi.VibrarExito, vm.eventos.first())
        assertEquals(EventoUi.NotificarRegistro("Ana Prueba"), vm.eventos.first())
    }

    @Test
    fun con_notificaciones_desactivadas_no_se_notifica() = runBlocking {
        preferencias.guardarNotificaciones(false)
        val vm = vm()
        vm.llenar()
        vm.enviar(null)
        assertEquals(EventoUi.VibrarExito, vm.eventos.first())
        // el registro se envía igual; solo se omite la notificación local
        assertEquals(1, api.enviados.size)
    }

    @Test
    fun precargar_desde_un_programa_guarda_tipo_referencia_e_ids() {
        val vm = vm()
        vm.precargar(TipoColaboracion.COMPRA, "Taller de producción", programaId = "3", productoId = null)
        val e = vm.estado.value
        assertEquals(TipoColaboracion.COMPRA, e.tipo)
        assertEquals("Me interesa: Taller de producción", e.mensaje)
        assertEquals("3", e.programaId)
        assertNull(e.productoId)
    }

    @Test
    fun precargar_no_pisa_un_mensaje_escrito_por_la_persona() {
        val vm = vm()
        vm.cambiarMensaje("Mi propio mensaje")
        vm.precargar(null, "Otra cosa")
        assertEquals("Mi propio mensaje", vm.estado.value.mensaje)
        vm.cambiarMensaje("")
        vm.precargar(null, "Otra cosa")
        assertEquals("Me interesa: Otra cosa", vm.estado.value.mensaje)
    }

    @Test
    fun los_ids_precargados_viajan_en_la_solicitud() = runBlocking {
        val vm = vm()
        vm.llenar()
        vm.precargar(TipoColaboracion.VOLUNTARIADO, "Formación para la autonomía", programaId = "1")
        vm.enviar(null)
        assertEquals(1L, api.enviados.single().programaId)
        assertNull(api.enviados.single().productoServicioId)
    }

    @Test
    fun donacion_consulta_el_dolar_y_calcula_el_equivalente() {
        val vm = vm()
        vm.cambiarTipo(TipoColaboracion.DONACION)
        vm.cambiarMonto("50.000abc")
        assertEquals("50000", vm.estado.value.monto)
        assertEquals(50.0, vm.estado.value.montoEnDolares!!, 0.0001)
    }

    @Test
    fun si_la_api_externa_falla_el_formulario_sigue_funcionando() {
        dolar = null
        val vm = vm()
        vm.cambiarTipo(TipoColaboracion.DONACION)
        vm.cambiarMonto("50000")
        assertNull(vm.estado.value.tipoCambio)
        assertNull(vm.estado.value.montoEnDolares)
    }

    @Test
    fun precargar_usuario_solo_completa_campos_vacios() {
        val vm = vm()
        vm.cambiarNombre("Nombre Escrito")
        vm.precargarUsuario("Nombre Cuenta", "cuenta@example.cl")
        assertEquals("Nombre Escrito", vm.estado.value.nombre)
        assertEquals("cuenta@example.cl", vm.estado.value.correo)
    }

    @Test
    fun registrar_otro_conserva_datos_de_contacto_y_limpia_el_resto() {
        val vm = vm()
        vm.llenar()
        vm.cambiarEsEmpresa(true)
        vm.cambiarOrganizacion("Empresa Demo")
        vm.enviar(null)
        vm.registrarOtro()
        val e = vm.estado.value
        assertEquals("Ana Prueba", e.nombre)
        assertEquals("Empresa Demo", e.organizacion)
        assertNull(e.tipo)
        assertFalse(e.guardadoExitoso)
        assertFalse(e.aceptaTerminos)
    }

    @Test
    fun respuesta_invalida_del_servidor_deja_pendiente_pero_no_pierde_el_registro() {
        api.crear = { EstadoPublicoDto(null, null, null, null) }
        val vm = vm()
        vm.llenar()
        vm.enviar(null)
        assertTrue(vm.estado.value.guardadoExitoso)
        assertFalse(dao.todos().single().sincronizado)
    }
}
