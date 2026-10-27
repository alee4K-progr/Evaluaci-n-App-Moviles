package com.corpaliv.albalab.viewmodel

/**
 * Eventos de una sola vez que el ViewModel le pide a la interfaz (mensajes, vibración, notificación).
 * Así el ViewModel no depende de Context ni de componentes visuales.
 */
sealed interface EventoUi {
    data class Mensaje(val texto: String) : EventoUi
    data object VibrarExito : EventoUi
    data object VibrarError : EventoUi
    data class NotificarRegistro(val nombre: String) : EventoUi
}
