package com.corpaliv.albalab.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.corpaliv.albalab.nativo.Haptico
import com.corpaliv.albalab.nativo.NotificadorLocal
import com.corpaliv.albalab.viewmodel.EventoUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Traduce los eventos del ViewModel a acciones de la interfaz y de los recursos nativos
 * (mensaje, vibración, notificación local).
 */
@Composable
fun RecolectarEventos(eventos: Flow<EventoUi>, snackbar: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(eventos) {
        eventos.collect { evento ->
            when (evento) {
                is EventoUi.Mensaje -> scope.launch {
                    snackbar.currentSnackbarData?.dismiss()
                    snackbar.showSnackbar(evento.texto)
                }
                EventoUi.VibrarExito -> Haptico.exito(context)
                EventoUi.VibrarError -> Haptico.error(context)
                is EventoUi.NotificarRegistro ->
                    NotificadorLocal.mostrarRegistroExitoso(context, evento.nombre)
            }
        }
    }
}
