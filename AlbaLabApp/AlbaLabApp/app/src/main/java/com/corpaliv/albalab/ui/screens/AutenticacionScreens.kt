package com.corpaliv.albalab.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.corpaliv.albalab.ui.components.AvisoDatosFicticios
import com.corpaliv.albalab.ui.components.BotonPrincipal
import com.corpaliv.albalab.ui.components.BotonSecundario
import com.corpaliv.albalab.ui.components.CampoValidado
import com.corpaliv.albalab.ui.components.CasillaValidada
import com.corpaliv.albalab.ui.components.ContenedorPantalla
import com.corpaliv.albalab.ui.components.LogoAlba
import com.corpaliv.albalab.viewmodel.AuthViewModel
import com.corpaliv.albalab.viewmodel.CampoLogin
import com.corpaliv.albalab.viewmodel.CampoRegistro

/** Mensaje general de error del formulario (por ejemplo "credenciales incorrectas"). */
@Composable
private fun BannerError(mensaje: String?) {
    // El último mensaje se mantiene mientras dura la animación de salida.
    val visible = mensaje != null
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Error, contentDescription = "Error", modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = mensaje ?: "", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun SplashScreen() {
    // Animación de entrada del logo: crece con un pequeño rebote y aparece suavemente.
    var inicio by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { inicio = true }
    val escala by animateFloatAsState(
        targetValue = if (inicio) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "escalaSplash"
    )
    val transparencia by animateFloatAsState(
        targetValue = if (inicio) 1f else 0f,
        animationSpec = tween(900),
        label = "alphaSplash"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                    alpha = transparencia
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoAlba(tamano = 140.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Alba Lab",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Inclusión y vinculación con impacto social",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LoginScreen(
    authVm: AuthViewModel,
    onIrARegistro: () -> Unit
) {
    val estado by authVm.login.collectAsStateWithLifecycle()

    ContenedorPantalla {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoAlba(tamano = 96.dp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Bienvenido/a",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Ingresa para guardar tus registros y tu perfil, o continúa como invitado.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        BannerError(estado.errorGeneral)

        CampoValidado(
            valor = estado.correo,
            onValorChange = authVm::loginCorreo,
            etiqueta = "Correo electrónico",
            iconoInicial = Icons.Filled.Email,
            error = estado.error(CampoLogin.CORREO),
            mostrarError = estado.mostrarError(CampoLogin.CORREO),
            teclado = KeyboardType.Email,
            onPerderFoco = { authVm.loginTocar(CampoLogin.CORREO) }
        )

        CampoValidado(
            valor = estado.clave,
            onValorChange = authVm::loginClave,
            etiqueta = "Contraseña",
            iconoInicial = Icons.Filled.Lock,
            error = estado.error(CampoLogin.CLAVE),
            mostrarError = estado.mostrarError(CampoLogin.CLAVE),
            esClave = true,
            claveVisible = estado.mostrarClave,
            onAlternarClave = authVm::loginAlternarClave,
            imeAction = ImeAction.Done,
            onPerderFoco = { authVm.loginTocar(CampoLogin.CLAVE) },
            onListo = authVm::ingresar
        )

        BotonPrincipal(
            texto = "Ingresar",
            icono = Icons.AutoMirrored.Filled.Login,
            cargando = estado.cargando,
            onClick = authVm::ingresar
        )

        TextButton(
            onClick = onIrARegistro,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿No tienes cuenta? Crear cuenta")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "  o  ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        BotonSecundario(
            texto = "Continuar como invitado",
            icono = Icons.Filled.Person,
            onClick = authVm::continuarComoInvitado
        )

        AvisoDatosFicticios()
    }
}

@Composable
fun RegistroScreen(
    authVm: AuthViewModel,
    onVolver: () -> Unit
) {
    val estado by authVm.registro.collectAsStateWithLifecycle()

    ContenedorPantalla {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tu contraseña se guarda cifrada en este dispositivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        BannerError(estado.errorGeneral)

        CampoValidado(
            valor = estado.nombre,
            onValorChange = authVm::registroNombre,
            etiqueta = "Nombre completo",
            iconoInicial = Icons.Filled.Person,
            error = estado.error(CampoRegistro.NOMBRE),
            mostrarError = estado.mostrarError(CampoRegistro.NOMBRE),
            capitalizacion = KeyboardCapitalization.Words,
            onPerderFoco = { authVm.registroTocar(CampoRegistro.NOMBRE) }
        )

        CampoValidado(
            valor = estado.correo,
            onValorChange = authVm::registroCorreo,
            etiqueta = "Correo electrónico",
            iconoInicial = Icons.Filled.Email,
            error = estado.error(CampoRegistro.CORREO),
            mostrarError = estado.mostrarError(CampoRegistro.CORREO),
            teclado = KeyboardType.Email,
            onPerderFoco = { authVm.registroTocar(CampoRegistro.CORREO) }
        )

        CampoValidado(
            valor = estado.clave,
            onValorChange = authVm::registroClave,
            etiqueta = "Contraseña",
            iconoInicial = Icons.Filled.Lock,
            error = estado.error(CampoRegistro.CLAVE),
            mostrarError = estado.mostrarError(CampoRegistro.CLAVE),
            ayuda = "Mínimo 8 caracteres, con mayúscula, minúscula y número",
            esClave = true,
            claveVisible = estado.mostrarClave,
            onAlternarClave = authVm::registroAlternarClave,
            onPerderFoco = { authVm.registroTocar(CampoRegistro.CLAVE) }
        )

        CampoValidado(
            valor = estado.confirmacion,
            onValorChange = authVm::registroConfirmacion,
            etiqueta = "Repite la contraseña",
            iconoInicial = Icons.Filled.Lock,
            error = estado.error(CampoRegistro.CONFIRMACION),
            mostrarError = estado.mostrarError(CampoRegistro.CONFIRMACION),
            esClave = true,
            claveVisible = estado.mostrarClave,
            onAlternarClave = authVm::registroAlternarClave,
            imeAction = ImeAction.Done,
            onPerderFoco = { authVm.registroTocar(CampoRegistro.CONFIRMACION) },
            onListo = authVm::crearCuenta
        )

        CasillaValidada(
            marcada = estado.aceptaTerminos,
            onCambio = authVm::registroTerminos,
            texto = "Acepto que mis datos se usen para gestionar mi cuenta en la app",
            error = estado.error(CampoRegistro.TERMINOS),
            mostrarError = estado.mostrarError(CampoRegistro.TERMINOS)
        )

        BotonPrincipal(
            texto = "Crear cuenta",
            icono = Icons.Filled.PersonAdd,
            cargando = estado.cargando,
            onClick = authVm::crearCuenta
        )

        TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("Ya tengo cuenta: volver a ingresar")
        }

        AvisoDatosFicticios()
    }
}
