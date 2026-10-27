package com.corpaliv.albalab.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.corpaliv.albalab.R
import com.corpaliv.albalab.domain.model.TipoColaboracion
import java.io.File

/** Ícono que representa cada modalidad de colaboración. */
fun TipoColaboracion.icono(): ImageVector = when (this) {
    TipoColaboracion.DONACION -> Icons.Filled.Favorite
    TipoColaboracion.ALIANZA -> Icons.Filled.Groups
    TipoColaboracion.VOLUNTARIADO -> Icons.Filled.VolunteerActivism
    TipoColaboracion.COMPRA -> Icons.Filled.ShoppingBag
    TipoColaboracion.CAPACITACION -> Icons.Filled.School
}

/** Botón principal: se achica al presionarlo y muestra un indicador mientras trabaja. */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    cargando: Boolean = false,
    habilitado: Boolean = true
) {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (presionado) 0.96f else 1f,
        animationSpec = tween(120),
        label = "escalaBoton"
    )
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        interactionSource = interaccion,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
    ) {
        AnimatedContent(targetState = cargando, label = "contenidoBoton") { trabajando ->
            if (trabajando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icono != null) {
                        Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(texto, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

/** Logo vectorial propio de la app. */
@Composable
fun LogoAlba(modifier: Modifier = Modifier, tamano: Dp = 96.dp) {
    Image(
        painter = painterResource(id = R.drawable.logo_alba),
        contentDescription = "Logo de Alba Lab",
        modifier = modifier.size(tamano)
    )
}

/** Foto de perfil circular (archivo local) o ícono por defecto. */
@Composable
fun ImagenPerfil(
    ruta: String?,
    modifier: Modifier = Modifier,
    tamano: Dp = 140.dp
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (ruta != null) {
            AsyncImage(
                model = File(ruta),
                contentDescription = "Foto de perfil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Sin foto de perfil",
                modifier = Modifier.size(tamano / 2),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Número que "cuenta" desde 0 hasta su valor al aparecer en pantalla. */
@Composable
fun NumeroAnimado(
    valor: Int,
    modifier: Modifier = Modifier,
    sufijo: String = "",
    estilo: TextStyle = MaterialTheme.typography.headlineMedium
) {
    var iniciar by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { iniciar = true }
    val mostrado by animateIntAsState(
        targetValue = if (iniciar) valor else 0,
        animationSpec = tween(durationMillis = 1200),
        label = "contador"
    )
    Text(text = "$mostrado$sufijo", style = estilo, modifier = modifier)
}

/** Título de sección con una línea descriptiva opcional. */
@Composable
fun TituloSeccion(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitulo != null) {
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Aviso fijo: los datos del MVP son de prueba (restricción de protección de datos del caso). */
@Composable
fun AvisoDatosFicticios(
    modifier: Modifier = Modifier,
    texto: String = "Versión de prueba: el contenido y las cifras son ilustrativos. No ingreses datos personales reales."
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = texto, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Tarjeta que se expande/contrae con animación al tocarla. */
@Composable
fun TarjetaAnimada(
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(250)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            contenido()
        }
    }
}

/**
 * Contenedor de pantalla con scroll: limita el ancho en pantallas grandes o en horizontal
 * (diseño adaptable) y separa los bloques de forma uniforme.
 */
@Composable
fun ContenedorPantalla(
    modifier: Modifier = Modifier,
    contenido: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = contenido
        )
    }
}
