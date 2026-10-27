package com.corpaliv.albalab.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corpaliv.albalab.domain.model.TipoColaboracion
import com.corpaliv.albalab.domain.validation.Validadores
import com.corpaliv.albalab.nativo.NotificadorLocal
import com.corpaliv.albalab.ui.components.AvisoDatosFicticios
import com.corpaliv.albalab.ui.components.BotonPrincipal
import com.corpaliv.albalab.ui.components.BotonSecundario
import com.corpaliv.albalab.ui.components.CampoComuna
import com.corpaliv.albalab.ui.components.CampoFecha
import com.corpaliv.albalab.ui.components.CampoValidado
import com.corpaliv.albalab.ui.components.CasillaValidada
import com.corpaliv.albalab.ui.components.ContenedorPantalla
import com.corpaliv.albalab.ui.components.FilaInterruptor
import com.corpaliv.albalab.ui.components.RecolectarEventos
import com.corpaliv.albalab.ui.components.SelectorTipo
import com.corpaliv.albalab.ui.components.TituloSeccion
import com.corpaliv.albalab.ui.theme.colorExito
import com.corpaliv.albalab.viewmodel.CampoInteres
import com.corpaliv.albalab.viewmodel.EstadoSesion
import com.corpaliv.albalab.viewmodel.Fabrica
import com.corpaliv.albalab.viewmodel.InteresUiState
import com.corpaliv.albalab.viewmodel.InteresViewModel

/**
 * Formulario principal: convierte el interés del visitante en un registro concreto de colaboración.
 * Incluye campos de texto (String), numérico (Int), fecha (Date), lista, filtro dinámico,
 * botones de opción, checkbox y switch, todos con validación visual.
 */
@Composable
fun ColaborarScreen(
    tipoInicial: TipoColaboracion?,
    referencia: String,
    sesion: EstadoSesion,
    snackbar: SnackbarHostState,
    onVerRegistros: () -> Unit,
    vm: InteresViewModel = viewModel(factory = Fabrica.instancia)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    RecolectarEventos(vm.eventos, snackbar)

    LaunchedEffect(tipoInicial, referencia) {
        if (tipoInicial != null || referencia.isNotBlank()) vm.precargar(tipoInicial, referencia)
    }
    LaunchedEffect(sesion) {
        if (sesion is EstadoSesion.Autenticado) {
            vm.precargarUsuario(sesion.usuario.nombre, sesion.usuario.correo)
        }
    }

    AnimatedContent(
        targetState = estado.guardadoExitoso,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "formularioOConfirmacion"
    ) { exito ->
        if (exito) {
            Confirmacion(
                nombre = estado.nombre,
                onOtro = vm::registrarOtro,
                onVerRegistros = onVerRegistros
            )
        } else {
            Formulario(estado = estado, vm = vm, sesion = sesion)
        }
    }
}

@Composable
private fun Formulario(
    estado: InteresUiState,
    vm: InteresViewModel,
    sesion: EstadoSesion
) {
    val context = LocalContext.current
    // Permiso de notificaciones (Android 13+): se pide cuando la persona activa el aviso.
    val permisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Si lo rechaza, el registro se guarda igual; solo no se mostrará la notificación. */ }

    ContenedorPantalla {
        TituloSeccion(
            titulo = "Quiero colaborar",
            subtitulo = "Déjanos tus datos y el equipo de vinculación te contactará"
        )
        AvisoDatosFicticios()

        // ---- Sobre ti ----
        Text("Sobre ti", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !estado.esEmpresa,
                onClick = { vm.cambiarEsEmpresa(false) },
                label = { Text("Persona") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            FilterChip(
                selected = estado.esEmpresa,
                onClick = { vm.cambiarEsEmpresa(true) },
                label = { Text("Empresa") },
                leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        CampoValidado(
            valor = estado.nombre,
            onValorChange = vm::cambiarNombre,
            etiqueta = if (estado.esEmpresa) "Nombre de contacto" else "Nombre completo",
            iconoInicial = Icons.Filled.Person,
            error = estado.error(CampoInteres.NOMBRE),
            mostrarError = estado.mostrarError(CampoInteres.NOMBRE),
            capitalizacion = KeyboardCapitalization.Words,
            onPerderFoco = { vm.tocarCampo(CampoInteres.NOMBRE) }
        )

        AnimatedVisibility(
            visible = estado.esEmpresa,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            CampoValidado(
                valor = estado.organizacion,
                onValorChange = vm::cambiarOrganizacion,
                etiqueta = "Nombre de la empresa u organización",
                iconoInicial = Icons.Filled.Business,
                error = estado.error(CampoInteres.ORGANIZACION),
                mostrarError = estado.mostrarError(CampoInteres.ORGANIZACION),
                capitalizacion = KeyboardCapitalization.Words,
                onPerderFoco = { vm.tocarCampo(CampoInteres.ORGANIZACION) }
            )
        }

        CampoValidado(
            valor = estado.correo,
            onValorChange = vm::cambiarCorreo,
            etiqueta = "Correo de contacto",
            iconoInicial = Icons.Filled.Email,
            error = estado.error(CampoInteres.CORREO),
            mostrarError = estado.mostrarError(CampoInteres.CORREO),
            teclado = KeyboardType.Email,
            onPerderFoco = { vm.tocarCampo(CampoInteres.CORREO) }
        )

        CampoValidado(
            valor = estado.telefono,
            onValorChange = vm::cambiarTelefono,
            etiqueta = "Teléfono móvil (opcional)",
            iconoInicial = Icons.Filled.Phone,
            error = estado.error(CampoInteres.TELEFONO),
            mostrarError = estado.mostrarError(CampoInteres.TELEFONO),
            teclado = KeyboardType.Phone,
            ayuda = "Ejemplo: 9 1234 5678",
            onPerderFoco = { vm.tocarCampo(CampoInteres.TELEFONO) }
        )

        CampoComuna(
            valor = estado.comuna,
            onValorChange = vm::cambiarComuna,
            error = estado.error(CampoInteres.COMUNA),
            mostrarError = estado.mostrarError(CampoInteres.COMUNA),
            onPerderFoco = { vm.tocarCampo(CampoInteres.COMUNA) }
        )

        HorizontalDivider()

        // ---- Tu colaboración ----
        Text("Tu colaboración", style = MaterialTheme.typography.titleMedium)

        SelectorTipo(
            seleccionado = estado.tipo,
            onSeleccionar = vm::cambiarTipo,
            error = estado.error(CampoInteres.TIPO),
            mostrarError = estado.mostrarError(CampoInteres.TIPO)
        )

        AnimatedVisibility(
            visible = estado.tipo?.requiereMonto == true,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            CampoValidado(
                valor = estado.monto,
                onValorChange = vm::cambiarMonto,
                etiqueta = "Monto del aporte (CLP)",
                iconoInicial = Icons.Filled.AttachMoney,
                error = estado.error(CampoInteres.MONTO),
                mostrarError = estado.mostrarError(CampoInteres.MONTO),
                teclado = KeyboardType.Number,
                ayuda = "Entre $${Validadores.formatoMiles(Validadores.MONTO_MINIMO)} y " +
                    "$${Validadores.formatoMiles(Validadores.MONTO_MAXIMO)}",
                onPerderFoco = { vm.tocarCampo(CampoInteres.MONTO) }
            )
        }

        CampoFecha(
            fecha = estado.fecha,
            onFecha = vm::cambiarFecha,
            error = estado.error(CampoInteres.FECHA),
            mostrarError = estado.mostrarError(CampoInteres.FECHA)
        )

        CampoValidado(
            valor = estado.mensaje,
            onValorChange = vm::cambiarMensaje,
            etiqueta = "Mensaje (opcional)",
            iconoInicial = Icons.Filled.Edit,
            error = estado.error(CampoInteres.MENSAJE),
            mostrarError = estado.mostrarError(CampoInteres.MENSAJE),
            lineas = 3,
            maxCaracteres = Validadores.LARGO_MAXIMO_MENSAJE,
            capitalizacion = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Default,
            onPerderFoco = { vm.tocarCampo(CampoInteres.MENSAJE) }
        )

        HorizontalDivider()

        // ---- Preferencias y consentimiento ----
        FilaInterruptor(
            titulo = "Quiero recibir novedades",
            descripcion = "Te avisaremos de nuevas campañas y actividades de Alba Lab",
            activo = estado.recibeNovedades,
            onCambio = { activo ->
                vm.cambiarNovedades(activo)
                if (activo && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !NotificadorLocal.tienePermiso(context)
                ) {
                    permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )

        CasillaValidada(
            marcada = estado.aceptaTerminos,
            onCambio = vm::cambiarTerminos,
            texto = "Acepto que Alba Lab use estos datos solo para contactarme sobre mi colaboración",
            error = estado.error(CampoInteres.TERMINOS),
            mostrarError = estado.mostrarError(CampoInteres.TERMINOS)
        )

        BotonPrincipal(
            texto = "Enviar mi interés",
            icono = Icons.AutoMirrored.Filled.Send,
            cargando = estado.guardando,
            onClick = {
                vm.enviar(if (sesion is EstadoSesion.Autenticado) sesion.usuario.correo else null)
            }
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Confirmacion(
    nombre: String,
    onOtro: () -> Unit,
    onVerRegistros: () -> Unit
) {
    // El ícono de éxito aparece con un rebote para reforzar que la acción se completó.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val escala by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "escalaExito"
    )

    ContenedorPantalla {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Registro enviado",
                tint = colorExito(),
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                    }
            )
            AnimatedVisibility(visible = visible, enter = fadeIn() + scaleIn()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "¡Gracias, ${nombre.substringBefore(' ')}!",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Guardamos tu interés de colaborar. El equipo de vinculación de Alba Lab " +
                            "te contactará en la fecha que indicaste.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            BotonPrincipal(texto = "Ver mis registros", onClick = onVerRegistros)
            BotonSecundario(texto = "Registrar otro interés", onClick = onOtro)
        }
    }
}
