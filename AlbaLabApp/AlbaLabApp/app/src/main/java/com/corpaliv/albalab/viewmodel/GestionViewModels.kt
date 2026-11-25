package com.corpaliv.albalab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corpaliv.albalab.data.remote.Resultado
import com.corpaliv.albalab.data.repository.GestionRepository
import com.corpaliv.albalab.domain.ReglasGestion
import com.corpaliv.albalab.domain.model.CambioEstado
import com.corpaliv.albalab.domain.model.EstadoSolicitud
import com.corpaliv.albalab.domain.model.SesionGestor
import com.corpaliv.albalab.domain.model.SolicitudGestion
import com.corpaliv.albalab.domain.validation.Validadores
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ------------------------------------------------------------------ Login del equipo de vinculación

data class GestorLoginUiState(
    val correo: String = "",
    val clave: String = "",
    val mostrarClave: Boolean = false,
    val intentoEnvio: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val ingresoExitoso: Boolean = false
) {
    val errorCorreo: String? get() = Validadores.correo(correo)
    val errorClave: String? get() = Validadores.claveIngreso(clave)
    val esValido: Boolean get() = errorCorreo == null && errorClave == null
}

class GestorLoginViewModel(private val repositorio: GestionRepository) : ViewModel() {
    private val _estado = MutableStateFlow(GestorLoginUiState())
    val estado: StateFlow<GestorLoginUiState> = _estado

    fun cambiarCorreo(v: String) = _estado.update { it.copy(correo = v, error = null) }
    fun cambiarClave(v: String) = _estado.update { it.copy(clave = v, error = null) }
    fun alternarClave() = _estado.update { it.copy(mostrarClave = !it.mostrarClave) }

    fun ingresar() {
        val actual = _estado.value
        if (actual.cargando) return
        _estado.update { it.copy(intentoEnvio = true) }
        if (!actual.esValido) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val r = repositorio.login(actual.correo, actual.clave)) {
                is Resultado.Ok -> _estado.update { it.copy(cargando = false, clave = "", ingresoExitoso = true) }
                is Resultado.Fallo -> _estado.update { it.copy(cargando = false, error = r.mensaje) }
            }
        }
    }
}

// ------------------------------------------------------------------ Lista de solicitudes

data class GestionUiState(
    val cargando: Boolean = false,
    val solicitudes: List<SolicitudGestion> = emptyList(),
    val filtro: EstadoSolicitud? = null,
    val error: String? = null
)

class GestionViewModel(private val repositorio: GestionRepository) : ViewModel() {
    private val _estado = MutableStateFlow(GestionUiState())
    val estado: StateFlow<GestionUiState> = _estado

    /** Pasa a null si el servidor rechaza el token (vencido); la pantalla vuelve al acceso. */
    val sesion: StateFlow<SesionGestor?> = repositorio.sesion

    // La pantalla llama a cargar() cada vez que vuelve a primer plano (p. ej. al regresar del detalle).
    fun cambiarFiltro(filtro: EstadoSolicitud?) {
        _estado.update { it.copy(filtro = filtro) }
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val r = repositorio.listar(_estado.value.filtro)) {
                is Resultado.Ok -> _estado.update { it.copy(cargando = false, solicitudes = r.valor) }
                is Resultado.Fallo -> _estado.update { it.copy(cargando = false, error = r.mensaje) }
            }
        }
    }

    fun cerrarSesion() = repositorio.cerrarSesion()
}

// ------------------------------------------------------------------ Detalle, cambio de estado y eliminación

data class DetalleUiState(
    val cargando: Boolean = true,
    val solicitud: SolicitudGestion? = null,
    val historial: List<CambioEstado> = emptyList(),
    val error: String? = null,
    val procesando: Boolean = false,
    val eliminada: Boolean = false
)

class SolicitudDetalleViewModel(private val repositorio: GestionRepository) : ViewModel() {
    private val _estado = MutableStateFlow(DetalleUiState())
    val estado: StateFlow<DetalleUiState> = _estado

    private val _eventos = Channel<EventoUi>(Channel.BUFFERED)
    val eventos: Flow<EventoUi> = _eventos.receiveAsFlow()

    private var idActual: Long? = null

    fun cargar(id: Long) {
        idActual = id
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            val detalle = repositorio.detalle(id)
            val historial = repositorio.historial(id)
            when {
                detalle is Resultado.Fallo -> _estado.update { it.copy(cargando = false, error = detalle.mensaje) }
                detalle is Resultado.Ok -> _estado.update {
                    it.copy(
                        cargando = false,
                        solicitud = detalle.valor,
                        historial = (historial as? Resultado.Ok)?.valor.orEmpty()
                    )
                }
            }
        }
    }

    /** Devuelve el error de validación (para mostrarlo en el diálogo) o null si se envió el cambio. */
    fun cambiarEstado(nuevo: EstadoSolicitud?, observacion: String): String? {
        val id = idActual ?: return "No hay solicitud cargada"
        val error = ReglasGestion.errorObservacion(nuevo, observacion)
        if (error != null || nuevo == null) return error
        if (_estado.value.procesando) return null
        viewModelScope.launch {
            _estado.update { it.copy(procesando = true) }
            when (val r = repositorio.cambiarEstado(id, nuevo, observacion)) {
                is Resultado.Ok -> {
                    val historial = repositorio.historial(id)
                    _estado.update {
                        it.copy(
                            procesando = false,
                            solicitud = r.valor,
                            historial = (historial as? Resultado.Ok)?.valor ?: it.historial
                        )
                    }
                    _eventos.trySend(EventoUi.VibrarExito)
                    _eventos.trySend(EventoUi.Mensaje("Estado actualizado a ${nuevo.etiqueta}"))
                }
                is Resultado.Fallo -> {
                    _estado.update { it.copy(procesando = false) }
                    _eventos.trySend(EventoUi.VibrarError)
                    _eventos.trySend(EventoUi.Mensaje(r.mensaje))
                }
            }
        }
        return null
    }

    fun eliminar() {
        val id = idActual ?: return
        if (_estado.value.procesando) return
        viewModelScope.launch {
            _estado.update { it.copy(procesando = true) }
            when (val r = repositorio.eliminar(id)) {
                is Resultado.Ok -> _estado.update { it.copy(procesando = false, eliminada = true) }
                is Resultado.Fallo -> {
                    _estado.update { it.copy(procesando = false) }
                    _eventos.trySend(EventoUi.Mensaje(r.mensaje))
                }
            }
        }
    }
}
