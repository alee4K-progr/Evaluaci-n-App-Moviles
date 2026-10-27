package com.corpaliv.albalab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corpaliv.albalab.data.local.UsuarioEntity
import com.corpaliv.albalab.data.preferences.SesionDataStore
import com.corpaliv.albalab.data.repository.ResultadoAuth
import com.corpaliv.albalab.data.repository.UsuarioRepository
import com.corpaliv.albalab.domain.validation.Validadores
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data object Invitado : EstadoSesion
    data class Autenticado(val usuario: UsuarioEntity) : EstadoSesion
}

enum class CampoLogin { CORREO, CLAVE }

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val mostrarClave: Boolean = false,
    val tocados: Set<CampoLogin> = emptySet(),
    val intentoEnvio: Boolean = false,
    val cargando: Boolean = false,
    val errorGeneral: String? = null
) {
    fun error(campo: CampoLogin): String? = when (campo) {
        CampoLogin.CORREO -> Validadores.correo(correo)
        CampoLogin.CLAVE -> Validadores.claveIngreso(clave)
    }

    /** El error se muestra recién cuando el usuario tocó el campo o intentó enviar. */
    fun mostrarError(campo: CampoLogin): Boolean = intentoEnvio || campo in tocados

    val esValido: Boolean get() = CampoLogin.entries.all { error(it) == null }
}

enum class CampoRegistro { NOMBRE, CORREO, CLAVE, CONFIRMACION, TERMINOS }

data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmacion: String = "",
    val mostrarClave: Boolean = false,
    val aceptaTerminos: Boolean = false,
    val tocados: Set<CampoRegistro> = emptySet(),
    val intentoEnvio: Boolean = false,
    val cargando: Boolean = false,
    val errorGeneral: String? = null
) {
    fun error(campo: CampoRegistro): String? = when (campo) {
        CampoRegistro.NOMBRE -> Validadores.nombre(nombre)
        CampoRegistro.CORREO -> Validadores.correo(correo)
        CampoRegistro.CLAVE -> Validadores.claveNueva(clave)
        CampoRegistro.CONFIRMACION -> Validadores.confirmarClave(clave, confirmacion)
        CampoRegistro.TERMINOS -> Validadores.terminos(aceptaTerminos)
    }

    fun mostrarError(campo: CampoRegistro): Boolean = intentoEnvio || campo in tocados

    val esValido: Boolean get() = CampoRegistro.entries.all { error(it) == null }
}

/** Maneja sesión, login y registro de cuenta (MVVM: la lógica vive aquí, no en las pantallas). */
class AuthViewModel(
    private val usuarios: UsuarioRepository,
    private val sesionDataStore: SesionDataStore
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val sesion: StateFlow<EstadoSesion> = sesionDataStore.sesion
        .flatMapLatest { datos ->
            val correo = datos.correo
            when {
                correo != null -> usuarios.observar(correo).map { usuario ->
                    if (usuario != null) EstadoSesion.Autenticado(usuario) else EstadoSesion.SinSesion
                }
                datos.invitado -> flowOf(EstadoSesion.Invitado)
                else -> flowOf(EstadoSesion.SinSesion)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoSesion.Cargando)

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _registro = MutableStateFlow(RegistroUiState())
    val registro: StateFlow<RegistroUiState> = _registro

    private val _eventos = Channel<EventoUi>(Channel.BUFFERED)
    val eventos: Flow<EventoUi> = _eventos.receiveAsFlow()

    // ---------- Login ----------

    fun loginCorreo(valor: String) = _login.update {
        it.copy(correo = valor, tocados = it.tocados + CampoLogin.CORREO, errorGeneral = null)
    }

    fun loginClave(valor: String) = _login.update {
        it.copy(clave = valor, tocados = it.tocados + CampoLogin.CLAVE, errorGeneral = null)
    }

    fun loginTocar(campo: CampoLogin) = _login.update { it.copy(tocados = it.tocados + campo) }

    fun loginAlternarClave() = _login.update { it.copy(mostrarClave = !it.mostrarClave) }

    fun ingresar() {
        val estado = _login.value
        if (estado.cargando) return
        _login.update { it.copy(intentoEnvio = true) }
        if (!estado.esValido) {
            _eventos.trySend(EventoUi.VibrarError)
            return
        }
        viewModelScope.launch {
            _login.update { it.copy(cargando = true, errorGeneral = null) }
            when (val r = usuarios.iniciarSesion(estado.correo, estado.clave)) {
                is ResultadoAuth.Exito -> {
                    sesionDataStore.iniciarSesion(r.correo)
                    _eventos.trySend(EventoUi.VibrarExito)
                    _login.value = LoginUiState()
                }
                is ResultadoAuth.Error -> {
                    _login.update { it.copy(cargando = false, errorGeneral = r.mensaje) }
                    _eventos.trySend(EventoUi.VibrarError)
                }
            }
        }
    }

    // ---------- Registro ----------

    fun registroNombre(valor: String) = _registro.update {
        it.copy(nombre = valor, tocados = it.tocados + CampoRegistro.NOMBRE, errorGeneral = null)
    }

    fun registroCorreo(valor: String) = _registro.update {
        it.copy(correo = valor, tocados = it.tocados + CampoRegistro.CORREO, errorGeneral = null)
    }

    fun registroClave(valor: String) = _registro.update {
        it.copy(clave = valor, tocados = it.tocados + CampoRegistro.CLAVE, errorGeneral = null)
    }

    fun registroConfirmacion(valor: String) = _registro.update {
        it.copy(confirmacion = valor, tocados = it.tocados + CampoRegistro.CONFIRMACION, errorGeneral = null)
    }

    fun registroTerminos(valor: Boolean) = _registro.update {
        it.copy(aceptaTerminos = valor, tocados = it.tocados + CampoRegistro.TERMINOS)
    }

    fun registroTocar(campo: CampoRegistro) = _registro.update { it.copy(tocados = it.tocados + campo) }

    fun registroAlternarClave() = _registro.update { it.copy(mostrarClave = !it.mostrarClave) }

    fun crearCuenta() {
        val estado = _registro.value
        if (estado.cargando) return
        _registro.update { it.copy(intentoEnvio = true) }
        if (!estado.esValido) {
            _eventos.trySend(EventoUi.VibrarError)
            return
        }
        viewModelScope.launch {
            _registro.update { it.copy(cargando = true, errorGeneral = null) }
            when (val r = usuarios.registrar(estado.nombre, estado.correo, estado.clave)) {
                is ResultadoAuth.Exito -> {
                    sesionDataStore.iniciarSesion(r.correo)
                    _eventos.trySend(EventoUi.VibrarExito)
                    _registro.value = RegistroUiState()
                }
                is ResultadoAuth.Error -> {
                    _registro.update { it.copy(cargando = false, errorGeneral = r.mensaje) }
                    _eventos.trySend(EventoUi.VibrarError)
                }
            }
        }
    }

    /** Limpia el formulario al volver a la pantalla anterior. */
    fun limpiarRegistro() {
        _registro.value = RegistroUiState()
    }

    // ---------- Sesión ----------

    fun continuarComoInvitado() {
        viewModelScope.launch { sesionDataStore.entrarComoInvitado() }
    }

    fun cerrarSesion() {
        viewModelScope.launch { sesionDataStore.cerrarSesion() }
    }
}
