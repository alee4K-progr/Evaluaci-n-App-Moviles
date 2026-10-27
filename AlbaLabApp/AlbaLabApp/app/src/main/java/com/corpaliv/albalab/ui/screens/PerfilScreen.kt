package com.corpaliv.albalab.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corpaliv.albalab.data.local.UsuarioEntity
import com.corpaliv.albalab.nativo.NotificadorLocal
import com.corpaliv.albalab.ui.components.AvisoDatosFicticios
import com.corpaliv.albalab.ui.components.BotonPrincipal
import com.corpaliv.albalab.ui.components.BotonSecundario
import com.corpaliv.albalab.ui.components.ContenedorPantalla
import com.corpaliv.albalab.ui.components.FilaInterruptor
import com.corpaliv.albalab.ui.components.ImagenPerfil
import com.corpaliv.albalab.ui.components.RecolectarEventos
import com.corpaliv.albalab.viewmodel.AuthViewModel
import com.corpaliv.albalab.viewmodel.EstadoSesion
import com.corpaliv.albalab.viewmodel.Fabrica
import com.corpaliv.albalab.viewmodel.PerfilViewModel

@Composable
fun PerfilScreen(
    sesion: EstadoSesion,
    authVm: AuthViewModel,
    snackbar: SnackbarHostState,
    onVerRegistros: () -> Unit,
    vm: PerfilViewModel = viewModel(factory = Fabrica.instancia)
) {
    RecolectarEventos(vm.eventos, snackbar)
    val notificaciones by vm.notificaciones.collectAsStateWithLifecycle()
    val procesando by vm.procesandoFoto.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val usuario: UsuarioEntity? = (sesion as? EstadoSesion.Autenticado)?.usuario
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }

    // ---- Recurso nativo 1: cámara (con permiso en tiempo de ejecución) ----
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    val camaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val ruta = rutaPendiente
        if (ruta != null && usuario != null) {
            vm.fotoCamaraTomada(usuario.correo, ruta, usuario.fotoPath, exito)
        }
        rutaPendiente = null
    }

    fun abrirCamara() {
        try {
            val (archivo, uri) = vm.prepararFotoCamara()
            rutaPendiente = archivo.absolutePath
            camaraLauncher.launch(uri)
        } catch (e: Exception) {
            vm.mostrarMensaje("No se pudo abrir la cámara")
        }
    }

    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara()
        else vm.mostrarMensaje("Sin permiso de cámara no se puede tomar la foto. Puedes elegir una de la galería.")
    }

    fun tomarFoto() {
        val permitido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (permitido) abrirCamara() else permisoCamara.launch(Manifest.permission.CAMERA)
    }

    // ---- Recurso nativo 2: galería de imágenes (selector del sistema, sin permisos amplios) ----
    val galeriaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (usuario != null) vm.fotoElegidaDeGaleria(usuario.correo, uri, usuario.fotoPath)
    }

    // ---- Recurso nativo 3: notificaciones (permiso en Android 13+) ----
    val permisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (!concedido) {
            vm.mostrarMensaje("Sin permiso, las confirmaciones no se mostrarán como notificación")
        }
    }

    ContenedorPantalla {
        if (usuario != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val rutaFoto = usuario.fotoPath?.takeIf { vm.fotoDisponible(it) }
                    ImagenPerfil(ruta = rutaFoto, tamano = 160.dp)
                    if (procesando) CircularProgressIndicator(modifier = Modifier.size(48.dp))
                }
                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = usuario.correo,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Foto de perfil", style = MaterialTheme.typography.titleMedium)
                    BotonPrincipal(
                        texto = "Tomar fotografía",
                        icono = Icons.Filled.CameraAlt,
                        onClick = ::tomarFoto
                    )
                    BotonSecundario(
                        texto = "Elegir de la galería",
                        icono = Icons.Filled.PhotoLibrary,
                        onClick = { galeriaLauncher.launch("image/*") }
                    )
                    if (usuario.fotoPath != null) {
                        BotonSecundario(
                            texto = "Quitar foto",
                            icono = Icons.Filled.Delete,
                            onClick = { vm.quitarFoto(usuario.correo, usuario.fotoPath) }
                        )
                    }
                    Text(
                        text = "La foto se guarda solo en este dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Invitado: se invita a crear una cuenta para guardar perfil y foto.
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Estás navegando como invitado",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Puedes enviar tu interés sin cuenta. Si creas una, podrás tener tu perfil con foto.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    BotonPrincipal(
                        texto = "Iniciar sesión o crear cuenta",
                        onClick = authVm::cerrarSesion
                    )
                }
            }
        }

        // ---- Preferencias ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Preferencias", style = MaterialTheme.typography.titleMedium)
                FilaInterruptor(
                    titulo = "Notificaciones de confirmación",
                    descripcion = "Mostrar un aviso cuando se guarda tu registro de interés",
                    activo = notificaciones,
                    onCambio = { activo ->
                        vm.cambiarNotificaciones(activo)
                        if (activo && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !NotificadorLocal.tienePermiso(context)
                        ) {
                            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }
        }

        BotonSecundario(
            texto = "Ver mis registros de interés",
            icono = Icons.Filled.Storefront,
            onClick = onVerRegistros
        )

        if (usuario != null) {
            HorizontalDivider()
            BotonSecundario(
                texto = "Cerrar sesión",
                icono = Icons.AutoMirrored.Filled.Logout,
                onClick = { confirmarSalida = true }
            )
        }

        AvisoDatosFicticios()
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            icon = { androidx.compose.material3.Icon(Icons.Filled.Info, contentDescription = null) },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tus registros y tu foto seguirán guardados en este dispositivo.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    authVm.cerrarSesion()
                }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") }
            }
        )
    }
}
