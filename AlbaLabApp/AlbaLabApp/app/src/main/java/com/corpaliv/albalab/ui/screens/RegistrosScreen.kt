package com.corpaliv.albalab.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corpaliv.albalab.data.local.RegistroInteresEntity
import com.corpaliv.albalab.domain.model.TipoColaboracion
import com.corpaliv.albalab.domain.validation.Validadores
import com.corpaliv.albalab.ui.components.BotonPrincipal
import com.corpaliv.albalab.ui.components.formatoCorto
import com.corpaliv.albalab.ui.components.icono
import com.corpaliv.albalab.viewmodel.Fabrica
import com.corpaliv.albalab.viewmodel.RegistrosViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@Composable
fun RegistrosScreen(
    onNuevoRegistro: () -> Unit,
    vm: RegistrosViewModel = viewModel(factory = Fabrica.instancia)
) {
    val registros by vm.registros.collectAsStateWithLifecycle()
    var aEliminar by rememberSaveable { mutableStateOf<Long?>(null) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        if (registros.isEmpty()) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Aún no has registrado ningún interés",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Cuando envíes el formulario de colaboración, tu registro aparecerá aquí.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                BotonPrincipal(texto = "Quiero colaborar", onClick = onNuevoRegistro)
            }
        } else {
            LazyColumn(
                modifier = Modifier.widthIn(max = 720.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(registros, key = { it.id }) { registro ->
                    TarjetaRegistro(registro, onEliminar = { aEliminar = registro.id })
                }
            }
        }
    }

    val idEliminar = aEliminar
    if (idEliminar != null) {
        AlertDialog(
            onDismissRequest = { aEliminar = null },
            title = { Text("¿Eliminar registro?") },
            text = { Text("Se borrará de este dispositivo. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.eliminar(idEliminar)
                    aEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { aEliminar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TarjetaRegistro(registro: RegistroInteresEntity, onEliminar: () -> Unit) {
    val tipo = TipoColaboracion.desdeNombre(registro.tipo)
    val fecha = Instant.ofEpochMilli(registro.fechaContacto).atZone(ZoneOffset.UTC).toLocalDate()
    val creado = Instant.ofEpochMilli(registro.creadoEn).atZone(ZoneId.systemDefault()).toLocalDate()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (tipo != null) {
                    Icon(tipo.icono(), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(8.dp))
                }
                Text(
                    text = tipo?.etiqueta ?: registro.tipo,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar registro")
                }
            }
            Text(
                text = if (registro.esEmpresa) "${registro.nombre} · ${registro.organizacion}" else registro.nombre,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${registro.correo} · ${registro.comuna}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (registro.monto != null) {
                Text(
                    text = "Aporte: $${Validadores.formatoMiles(registro.monto)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Contactar el ${fecha.formatoCorto()} · registrado el ${creado.formatoCorto()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (registro.mensaje.isNotBlank()) {
                Text(text = "“${registro.mensaje}”", style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = if (registro.sincronizado) "Enviado al equipo de vinculación"
                    else "Guardado en el dispositivo · pendiente de envío",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
