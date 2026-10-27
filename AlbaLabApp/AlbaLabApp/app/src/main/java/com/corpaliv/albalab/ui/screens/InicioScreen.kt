package com.corpaliv.albalab.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.corpaliv.albalab.data.catalogo.CatalogoDatos
import com.corpaliv.albalab.domain.model.DestinoAporte
import com.corpaliv.albalab.domain.model.TipoColaboracion
import com.corpaliv.albalab.ui.components.AvisoDatosFicticios
import com.corpaliv.albalab.ui.components.BotonPrincipal
import com.corpaliv.albalab.ui.components.BotonSecundario
import com.corpaliv.albalab.ui.components.ContenedorPantalla
import com.corpaliv.albalab.ui.components.NumeroAnimado
import com.corpaliv.albalab.ui.components.TituloSeccion
import com.corpaliv.albalab.ui.components.icono
import com.corpaliv.albalab.viewmodel.EstadoSesion

@Composable
fun InicioScreen(
    sesion: EstadoSesion,
    onColaborar: (TipoColaboracion?) -> Unit,
    onVerProgramas: () -> Unit
) {
    ContenedorPantalla {
        if (sesion is EstadoSesion.Autenticado) {
            Text(
                text = "Hola, ${sesion.usuario.nombre.substringBefore(' ')}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ---- Propuesta de valor ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Juntos construimos inclusión",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "CORPALIV promueve la autonomía, la participación y la inclusión social y laboral " +
                        "de personas con discapacidad múltiple, a través de la Escuela Especial Jan Van Dijk " +
                        "y la Red Sociolaboral Alba Lab.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                BotonPrincipal(
                    texto = "Quiero colaborar",
                    icono = Icons.Filled.VolunteerActivism,
                    onClick = { onColaborar(null) }
                )
                BotonSecundario(
                    texto = "Conocer los programas",
                    icono = Icons.Filled.School,
                    onClick = onVerProgramas
                )
            }
        }

        // ---- Indicadores de impacto ----
        TituloSeccion(titulo = "Nuestro impacto", subtitulo = "Cifras ilustrativas de la versión de prueba")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CatalogoDatos.indicadores.forEach { indicador ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        NumeroAnimado(
                            valor = indicador.valor,
                            sufijo = indicador.sufijo,
                            estilo = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = indicador.etiqueta,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ---- Formas de colaborar ----
        TituloSeccion(
            titulo = "Formas de colaborar",
            subtitulo = "Elige la que mejor se adapte a ti o a tu empresa"
        )
        TipoColaboracion.entries.forEach { tipo ->
            TarjetaModalidad(tipo = tipo, onClick = { onColaborar(tipo) })
        }

        // ---- Destino de los aportes ----
        TituloSeccion(
            titulo = "¿A dónde van tus aportes?",
            subtitulo = "Distribución ilustrativa"
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CatalogoDatos.destinoAportes.forEach { destino -> BarraDestino(destino) }
            }
        }

        AvisoDatosFicticios()
    }
}

@Composable
private fun TarjetaModalidad(tipo: TipoColaboracion, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Colaborar: ${tipo.etiqueta}", role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tipo.icono(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tipo.etiqueta, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = tipo.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** Barra de porcentaje que se llena con animación al aparecer. */
@Composable
private fun BarraDestino(destino: DestinoAporte) {
    var iniciar by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { iniciar = true }
    val progreso by animateFloatAsState(
        targetValue = if (iniciar) destino.porcentaje / 100f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "barraDestino"
    )
    Column(verticalArrangement = spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = destino.concepto, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${destino.porcentaje}%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
