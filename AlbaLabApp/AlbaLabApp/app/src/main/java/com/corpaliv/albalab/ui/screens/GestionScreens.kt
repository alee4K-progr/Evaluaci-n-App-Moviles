package com.corpaliv.albalab.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corpaliv.albalab.domain.ReglasGestion
import com.corpaliv.albalab.domain.model.CambioEstado
import com.corpaliv.albalab.domain.model.EstadoSolicitud
import com.corpaliv.albalab.domain.model.SolicitudGestion
import com.corpaliv.albalab.domain.validation.Validadores
import com.corpaliv.albalab.ui.components.AvisoDatosFicticios
import com.corpaliv.albalab.ui.components.BotonPrincipal
import com.corpaliv.albalab.ui.components.BotonSecundario
import com.corpaliv.albalab.ui.components.CampoValidado
import com.corpaliv.albalab.ui.components.ContenedorPantalla
import com.corpaliv.albalab.ui.components.RecolectarEventos
import com.corpaliv.albalab.ui.components.TituloSeccion
import com.corpaliv.albalab.viewmodel.Fabrica
import com.corpaliv.albalab.viewmodel.GestionViewModel
import com.corpaliv.albalab.viewmodel.GestorLoginViewModel
import com.corpaliv.albalab.viewmodel.SolicitudDetalleViewModel

// ------------------------------------------------------------------ Acceso del equipo de vinculación

@Composable
fun GestorLoginScreen(
    onIngreso: () -> Unit,
    vm: GestorLoginViewModel = viewModel(factory = Fabrica.instancia)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado.ingresoExitoso) { if (estado.ingresoExitoso) onIngreso() }

    ContenedorPantalla {
        TituloSeccion(
            titulo = "Acceso del equipo de vinculación",
            subtitulo = "Solo para gestores y administradores de CORPALIV / Alba Lab"
        )
        AvisoDatosFicticios(
            texto = "Demostración académica: usuarios de prueba gestor@example.cl (Gestor123) o admin@example.cl (Admin123). Son cuentas ficticias."
        )
        CampoValidado(
            valor = estado.correo,
            onValorChange = vm::cambiarCorreo,
            etiqueta = "Correo institucional",
            iconoInicial = Icons.Filled.Email,
            error = estado.errorCorreo,
            mostrarError = estado.intentoEnvio,
            teclado = KeyboardType.Email
        )
        CampoValidado(
            valor = estado.clave,
            onValorChange = vm::cambiarClave,
            etiqueta = "Contraseña",
            iconoInicial = Icons.Filled.Lock,
            error = estado.errorClave,
            mostrarError = estado.intentoEnvio,
            esClave = true,
            claveVisible = estado.mostrarClave,
            onAlternarClave = vm::alternarClave,
            onListo = vm::ingresar
        )
        if (estado.error != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = estado.error.orEmpty(), modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }
        BotonPrincipal(
            texto = "Ingresar",
            icono = Icons.Filled.AdminPanelSettings,
            onClick = vm::ingresar,
            cargando = estado.cargando,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ------------------------------------------------------------------ Lista de solicitudes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionScreen(
    onAbrir: (Long) -> Unit,
    onSesionCerrada: () -> Unit,
    vm: GestionViewModel = viewModel(factory = Fabrica.instancia)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val sesion by vm.sesion.collectAsStateWithLifecycle()

    // Si la sesión se cierra (botón o token vencido) se vuelve al perfil.
    LaunchedEffect(sesion) { if (sesion == null) onSesionCerrada() }
    // Recarga al abrir y cada vez que se vuelve desde el detalle.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { if (vm.sesion.value != null) vm.cargar() }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.widthIn(max = 720.dp).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(sesion?.nombre.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    Text("Rol: ${sesion?.rol.orEmpty()}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = vm::cargar) { Icon(Icons.Filled.Refresh, contentDescription = "Actualizar lista") }
                IconButton(onClick = vm::cerrarSesion) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión del equipo")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = estado.filtro == null, onClick = { vm.cambiarFiltro(null) }, label = { Text("Todas") })
                EstadoSolicitud.entries.forEach { e ->
                    FilterChip(selected = estado.filtro == e, onClick = { vm.cambiarFiltro(e) }, label = { Text(e.etiqueta) })
                }
            }
            Spacer(Modifier.height(8.dp))
            when {
                estado.cargando && estado.solicitudes.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                estado.error != null ->
                    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(estado.error.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(12.dp))
                        BotonSecundario(texto = "Reintentar", onClick = vm::cargar)
                    }
                estado.solicitudes.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay solicitudes con este filtro", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                else -> LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(estado.solicitudes, key = { it.id }) { s -> TarjetaSolicitud(s) { onAbrir(s.id) } }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaSolicitud(s: SolicitudGestion, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.codigo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                AssistChip(onClick = onClick, label = { Text(s.estado.etiqueta) })
            }
            Text(
                if (s.organizacion.isNullOrBlank()) s.nombreContacto else "${s.nombreContacto} · ${s.organizacion}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "${s.tipo?.etiqueta ?: "Otro"} · ${s.fechaCreacion.take(10)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ------------------------------------------------------------------ Detalle, cambio de estado y eliminación

@Composable
fun SolicitudDetalleScreen(
    id: Long,
    snackbar: SnackbarHostState,
    onVolver: () -> Unit,
    vm: SolicitudDetalleViewModel = viewModel(factory = Fabrica.instancia)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    RecolectarEventos(vm.eventos, snackbar)
    LaunchedEffect(id) { vm.cargar(id) }
    LaunchedEffect(estado.eliminada) { if (estado.eliminada) onVolver() }

    var cambioA by rememberSaveable { mutableStateOf<EstadoSolicitud?>(null) }
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }
    val solicitud = estado.solicitud

    when {
        estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        solicitud == null -> Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(estado.error ?: "No se pudo cargar la solicitud", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            BotonSecundario(texto = "Reintentar", onClick = { vm.cargar(id) })
        }
        else -> ContenedorPantalla {
            DatosSolicitud(solicitud)

            val siguientes = solicitud.estado.siguientes()
            Text("Cambiar estado", style = MaterialTheme.typography.titleMedium)
            if (siguientes.isEmpty()) {
                Text("Esta solicitud está en un estado final.", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    siguientes.forEach { e ->
                        BotonSecundario(texto = "Pasar a ${e.etiqueta}", onClick = { cambioA = e }, habilitado = !estado.procesando)
                    }
                }
            }

            HorizontalDivider()
            Text("Historial de cambios", style = MaterialTheme.typography.titleMedium)
            if (estado.historial.isEmpty()) Text("Sin registros", style = MaterialTheme.typography.bodyMedium)
            estado.historial.forEach { FilaHistorial(it) }

            HorizontalDivider()
            BotonSecundario(
                texto = "Eliminar solicitud",
                icono = Icons.Filled.Delete,
                onClick = { confirmarEliminar = true },
                habilitado = !estado.procesando
            )
        }
    }

    val nuevo = cambioA
    if (nuevo != null) {
        DialogoCambioEstado(
            nuevo = nuevo,
            onCancelar = { cambioA = null },
            onConfirmar = { obs -> vm.cambiarEstado(nuevo, obs).also { error -> if (error == null) cambioA = null } }
        )
    }
    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("¿Eliminar solicitud?") },
            text = { Text("Se borrará la solicitud y su historial del servidor. Esta acción no se puede deshacer.") },
            confirmButton = { TextButton(onClick = { confirmarEliminar = false; vm.eliminar() }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatosSolicitud(s: SolicitudGestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.codigo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text(s.estado.etiqueta) })
            }
            Text(s.nombreContacto, style = MaterialTheme.typography.bodyLarge)
            if (!s.organizacion.isNullOrBlank()) Text("Organización: ${s.organizacion}")
            if (!s.email.isNullOrBlank()) Text("Correo: ${s.email}")
            if (!s.telefono.isNullOrBlank()) Text("Teléfono: ${s.telefono}")
            Text("Interés: ${s.tipo?.etiqueta ?: "Otro"}")
            if (s.programaId != null) Text("Programa (id): ${s.programaId}")
            if (s.productoServicioId != null) Text("Producto o servicio (id): ${s.productoServicioId}")
            if (s.montoClp != null) Text("Aporte: $${Validadores.formatoMiles(s.montoClp)}", color = MaterialTheme.colorScheme.primary)
            if (!s.mensaje.isNullOrBlank()) Text("“${s.mensaje}”", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Creada: ${s.fechaCreacion.replace('T', ' ').take(16)} · Actualizada: ${s.fechaActualizacion.replace('T', ' ').take(16)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilaHistorial(c: CambioEstado) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            "${c.estadoAnterior?.etiqueta ?: "—"} → ${c.estadoNuevo.etiqueta}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            "${c.fecha.replace('T', ' ').take(16)} · ${c.usuario.orEmpty()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!c.observacion.isNullOrBlank()) Text(c.observacion, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DialogoCambioEstado(
    nuevo: EstadoSolicitud,
    onCancelar: () -> Unit,
    onConfirmar: (String) -> String?
) {
    var observacion by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Pasar a ${nuevo.etiqueta}") },
        text = {
            OutlinedTextField(
                value = observacion,
                onValueChange = { observacion = it.take(ReglasGestion.LARGO_MAXIMO_OBSERVACION + 20); error = null },
                label = { Text(if (nuevo == EstadoSolicitud.DESCARTADO) "Observación (obligatoria)" else "Observación interna (opcional)") },
                isError = error != null,
                supportingText = { Text(error ?: "${observacion.length}/${ReglasGestion.LARGO_MAXIMO_OBSERVACION}") },
                minLines = 2
            )
        },
        confirmButton = { TextButton(onClick = { error = onConfirmar(observacion) }) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
