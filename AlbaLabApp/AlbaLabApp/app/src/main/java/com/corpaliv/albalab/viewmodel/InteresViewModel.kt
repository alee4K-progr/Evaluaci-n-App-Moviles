package com.corpaliv.albalab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corpaliv.albalab.data.local.RegistroInteresEntity
import com.corpaliv.albalab.data.preferences.PreferenciasSesion
import com.corpaliv.albalab.data.repository.IndicadoresEconomicosRepository
import com.corpaliv.albalab.data.repository.InteresRepository
import com.corpaliv.albalab.domain.Conversion
import com.corpaliv.albalab.domain.model.TipoCambio
import com.corpaliv.albalab.domain.model.TipoColaboracion
import com.corpaliv.albalab.domain.validation.Validadores
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

enum class CampoInteres {
    NOMBRE, CORREO, TELEFONO, ORGANIZACION, COMUNA, TIPO, MONTO, FECHA, MENSAJE, TERMINOS
}

data class InteresUiState(
    val esEmpresa: Boolean = false,
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val organizacion: String = "",
    val comuna: String = "",
    val tipo: TipoColaboracion? = null,
    val monto: String = "",
    val fecha: LocalDate? = null,
    val mensaje: String = "",
    val aceptaTerminos: Boolean = false,
    val recibeNovedades: Boolean = false,
    val tocados: Set<CampoInteres> = emptySet(),
    val intentoEnvio: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    /** Dólar observado (API externa mindicador.cl); null mientras no se haya podido consultar. */
    val tipoCambio: TipoCambio? = null,
    /** Programa o producto desde el que se abrió el formulario (ids de ms-contenidos). */
    val programaId: String? = null,
    val productoId: String? = null
) {
    /** Equivalente en dólares del monto escrito, solo para donaciones con monto válido. */
    val montoEnDolares: Double?
        get() {
            val cambio = tipoCambio ?: return null
            if (tipo?.requiereMonto != true || Validadores.monto(monto, tipo) != null) return null
            return Conversion.clpADolares(monto.toInt(), cambio.clpPorDolar)
        }

    fun error(campo: CampoInteres): String? = when (campo) {
        CampoInteres.NOMBRE -> Validadores.nombre(nombre)
        CampoInteres.CORREO -> Validadores.correoOpcional(correo, telefono)
        CampoInteres.TELEFONO -> Validadores.telefonoOpcional(telefono, correo)
        CampoInteres.ORGANIZACION -> Validadores.organizacion(organizacion, esEmpresa)
        CampoInteres.COMUNA -> Validadores.comuna(comuna)
        CampoInteres.TIPO -> Validadores.tipo(tipo)
        CampoInteres.MONTO -> Validadores.monto(monto, tipo)
        CampoInteres.FECHA -> Validadores.fechaContacto(fecha)
        CampoInteres.MENSAJE -> Validadores.mensaje(mensaje)
        CampoInteres.TERMINOS -> Validadores.terminos(aceptaTerminos)
    }

    fun mostrarError(campo: CampoInteres): Boolean = intentoEnvio || campo in tocados

    val esValido: Boolean get() = CampoInteres.entries.all { error(it) == null }
}

/** Lógica del formulario de interés de colaboración (formulario principal de conversión). */
class InteresViewModel(
    private val repositorio: InteresRepository,
    private val sesionDataStore: PreferenciasSesion,
    private val indicadores: IndicadoresEconomicosRepository
) : ViewModel() {

    private companion object {
        const val PREFIJO_REFERENCIA = "Me interesa: "
    }

    private val _estado = MutableStateFlow(InteresUiState())
    val estado: StateFlow<InteresUiState> = _estado

    private val _eventos = Channel<EventoUi>(Channel.BUFFERED)
    val eventos: Flow<EventoUi> = _eventos.receiveAsFlow()

    private fun tocar(campo: CampoInteres, cambio: (InteresUiState) -> InteresUiState) =
        _estado.update { actual -> cambio(actual).let { nuevo -> nuevo.copy(tocados = nuevo.tocados + campo) } }

    fun tocarCampo(campo: CampoInteres) = _estado.update { it.copy(tocados = it.tocados + campo) }

    fun cambiarEsEmpresa(valor: Boolean) = _estado.update { it.copy(esEmpresa = valor) }

    fun cambiarNombre(v: String) = tocar(CampoInteres.NOMBRE) { it.copy(nombre = v) }

    // Correo y teléfono se validan en conjunto (basta con uno): al cambiar uno se vuelve a evaluar el otro.
    fun cambiarCorreo(v: String) =
        tocar(CampoInteres.CORREO) { it.copy(correo = v, tocados = it.tocados + CampoInteres.TELEFONO) }

    fun cambiarTelefono(v: String) =
        tocar(CampoInteres.TELEFONO) {
            it.copy(
                telefono = v.filter { c -> c.isDigit() || c == '+' || c == ' ' }.take(15),
                tocados = it.tocados + CampoInteres.CORREO
            )
        }

    fun cambiarOrganizacion(v: String) = tocar(CampoInteres.ORGANIZACION) { it.copy(organizacion = v) }

    fun cambiarComuna(v: String) = tocar(CampoInteres.COMUNA) { it.copy(comuna = v) }

    fun cambiarTipo(v: TipoColaboracion) {
        tocar(CampoInteres.TIPO) { it.copy(tipo = v) }
        if (v.requiereMonto) cargarTipoCambio()
    }

    private var consultandoCambio = false

    /** Consulta el dólar a la API externa una sola vez por pantalla; si falla, simplemente no se muestra. */
    private fun cargarTipoCambio() {
        if (consultandoCambio || _estado.value.tipoCambio != null) return
        consultandoCambio = true
        viewModelScope.launch {
            val cambio = indicadores.dolarObservado()
            _estado.update { it.copy(tipoCambio = cambio) }
            consultandoCambio = false
        }
    }

    fun cambiarMonto(v: String) =
        tocar(CampoInteres.MONTO) { it.copy(monto = v.filter { c -> c.isDigit() }.take(9)) }

    fun cambiarFecha(v: LocalDate?) = tocar(CampoInteres.FECHA) { it.copy(fecha = v) }

    fun cambiarMensaje(v: String) =
        tocar(CampoInteres.MENSAJE) { it.copy(mensaje = v.take(Validadores.LARGO_MAXIMO_MENSAJE + 20)) }

    fun cambiarTerminos(v: Boolean) = tocar(CampoInteres.TERMINOS) { it.copy(aceptaTerminos = v) }

    fun cambiarNovedades(v: Boolean) = _estado.update { it.copy(recibeNovedades = v) }

    /** Rellena el formulario con la opción elegida en otra pantalla (programa, producto, etc.). */
    fun precargar(
        tipo: TipoColaboracion?,
        referencia: String,
        programaId: String? = null,
        productoId: String? = null
    ) {
        _estado.update { actual ->
            // Solo se reemplaza el mensaje si está vacío o fue generado automáticamente antes.
            val mensajeAutomatico = actual.mensaje.isBlank() || actual.mensaje.startsWith(PREFIJO_REFERENCIA)
            actual.copy(
                programaId = programaId ?: actual.programaId,
                productoId = productoId ?: actual.productoId,
                tipo = tipo ?: actual.tipo,
                mensaje = if (referencia.isNotBlank() && mensajeAutomatico)
                    "$PREFIJO_REFERENCIA$referencia" else actual.mensaje
            )
        }
        if (tipo?.requiereMonto == true) cargarTipoCambio()
    }

    /** Si la persona inició sesión, se sugieren sus datos (solo si el campo está vacío). */
    fun precargarUsuario(nombre: String, correo: String) {
        _estado.update {
            it.copy(
                nombre = it.nombre.ifBlank { nombre },
                correo = it.correo.ifBlank { correo }
            )
        }
    }

    fun enviar(usuarioCorreo: String?) {
        val actual = _estado.value
        if (actual.guardando) return
        _estado.update { it.copy(intentoEnvio = true) }
        if (!actual.esValido) {
            _eventos.trySend(EventoUi.VibrarError)
            _eventos.trySend(EventoUi.Mensaje("Revisa los campos marcados en rojo"))
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            try {
                val fecha = requireNotNull(actual.fecha)
                val tipo = requireNotNull(actual.tipo)
                val resultado = repositorio.guardarYEnviar(
                    RegistroInteresEntity(
                        usuarioCorreo = usuarioCorreo,
                        nombre = actual.nombre.trim(),
                        correo = actual.correo.trim().lowercase(),
                        telefono = actual.telefono.trim(),
                        esEmpresa = actual.esEmpresa,
                        organizacion = if (actual.esEmpresa) actual.organizacion.trim() else "",
                        comuna = actual.comuna.trim(),
                        tipo = tipo.name,
                        monto = if (tipo.requiereMonto) actual.monto.toIntOrNull() else null,
                        fechaContacto = fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
                        mensaje = actual.mensaje.trim(),
                        aceptaTerminos = actual.aceptaTerminos,
                        recibeNovedades = actual.recibeNovedades,
                        programaId = actual.programaId,
                        productoId = actual.productoId
                    )
                )
                _estado.update { it.copy(guardando = false, guardadoExitoso = true) }
                _eventos.trySend(EventoUi.VibrarExito)
                if (!resultado.sincronizado) {
                    _eventos.trySend(
                        EventoUi.Mensaje("Registro guardado. Se enviará al equipo cuando haya conexión.")
                    )
                }
                if (sesionDataStore.notificacionesActivas.first()) {
                    _eventos.trySend(EventoUi.NotificarRegistro(actual.nombre.trim()))
                }
            } catch (e: Exception) {
                _estado.update { it.copy(guardando = false) }
                _eventos.trySend(EventoUi.VibrarError)
                _eventos.trySend(EventoUi.Mensaje("No se pudo guardar tu registro. Inténtalo nuevamente."))
            }
        }
    }

    /** Vuelve a un formulario vacío (conserva nombre y correo para registrar otro interés). */
    fun registrarOtro() {
        _estado.update {
            InteresUiState(
                nombre = it.nombre, correo = it.correo, esEmpresa = it.esEmpresa,
                organizacion = it.organizacion, tipoCambio = it.tipoCambio
            )
        }
    }
}
