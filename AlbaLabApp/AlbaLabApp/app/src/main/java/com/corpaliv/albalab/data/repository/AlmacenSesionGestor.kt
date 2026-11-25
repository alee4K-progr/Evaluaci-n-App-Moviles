package com.corpaliv.albalab.data.repository

import com.corpaliv.albalab.domain.model.SesionGestor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Guarda la sesión del equipo de vinculación SOLO en memoria (no en disco): el token JWT no queda
 * almacenado sin cifrar en el teléfono y, al cerrar la app, hay que volver a iniciar sesión.
 */
class AlmacenSesionGestor {
    private val _sesion = MutableStateFlow<SesionGestor?>(null)
    val sesion: StateFlow<SesionGestor?> = _sesion

    val token: String? get() = _sesion.value?.token

    fun guardar(sesion: SesionGestor) { _sesion.value = sesion }

    fun cerrar() { _sesion.value = null }
}
