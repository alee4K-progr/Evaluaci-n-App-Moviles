package com.corpaliv.albalab.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corpaliv.albalab.data.preferences.SesionDataStore
import com.corpaliv.albalab.data.repository.AlmacenFotos
import com.corpaliv.albalab.data.repository.UsuarioRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

/** Perfil: foto (cámara / galería) y preferencia de notificaciones, ambas persistentes. */
class PerfilViewModel(
    private val usuarios: UsuarioRepository,
    private val fotos: AlmacenFotos,
    private val sesionDataStore: SesionDataStore
) : ViewModel() {

    val notificaciones: StateFlow<Boolean> = sesionDataStore.notificacionesActivas
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _procesandoFoto = MutableStateFlow(false)
    val procesandoFoto: StateFlow<Boolean> = _procesandoFoto

    private val _eventos = Channel<EventoUi>(Channel.BUFFERED)
    val eventos: Flow<EventoUi> = _eventos.receiveAsFlow()

    fun prepararFotoCamara(): Pair<File, Uri> = fotos.prepararFotoCamara()

    fun fotoCamaraTomada(correo: String, rutaNueva: String, rutaAnterior: String?, exito: Boolean) {
        if (!exito) {
            fotos.eliminar(rutaNueva)
            return
        }
        viewModelScope.launch {
            usuarios.actualizarFoto(correo, rutaNueva)
            if (rutaAnterior != rutaNueva) fotos.eliminar(rutaAnterior)
            _eventos.trySend(EventoUi.VibrarExito)
            _eventos.trySend(EventoUi.Mensaje("Foto de perfil actualizada"))
        }
    }

    fun fotoElegidaDeGaleria(correo: String, uri: Uri?, rutaAnterior: String?) {
        if (uri == null) return
        viewModelScope.launch {
            _procesandoFoto.value = true
            val archivo = fotos.copiarDesdeGaleria(uri)
            if (archivo != null) {
                usuarios.actualizarFoto(correo, archivo.absolutePath)
                fotos.eliminar(rutaAnterior)
                _eventos.trySend(EventoUi.VibrarExito)
                _eventos.trySend(EventoUi.Mensaje("Foto de perfil actualizada"))
            } else {
                _eventos.trySend(EventoUi.VibrarError)
                _eventos.trySend(EventoUi.Mensaje("No se pudo usar esa imagen"))
            }
            _procesandoFoto.value = false
        }
    }

    fun quitarFoto(correo: String, rutaActual: String?) {
        viewModelScope.launch {
            usuarios.actualizarFoto(correo, null)
            fotos.eliminar(rutaActual)
            _eventos.trySend(EventoUi.Mensaje("Foto eliminada"))
        }
    }

    fun cambiarNotificaciones(activas: Boolean) {
        viewModelScope.launch { sesionDataStore.guardarNotificaciones(activas) }
    }

    fun mostrarMensaje(texto: String) {
        _eventos.trySend(EventoUi.Mensaje(texto))
    }

    fun fotoDisponible(ruta: String?): Boolean = ruta != null && fotos.existe(ruta)
}
