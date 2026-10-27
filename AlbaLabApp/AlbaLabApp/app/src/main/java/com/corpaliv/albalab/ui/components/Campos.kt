package com.corpaliv.albalab.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.vector.ImageVector
import com.corpaliv.albalab.domain.Filtros
import com.corpaliv.albalab.domain.model.TipoColaboracion
import com.corpaliv.albalab.ui.theme.colorExito
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun LocalDate.formatoCorto(): String = format(FORMATO_FECHA)

/**
 * Texto de apoyo bajo un campo: muestra el mensaje de error con un ícono (animado),
 * o una ayuda neutra, y opcionalmente un contador de caracteres.
 */
@Composable
fun ApoyoCampo(
    error: String?,
    mostrarError: Boolean,
    ayuda: String? = null,
    contador: Pair<Int, Int>? = null
) {
    val hayError = mostrarError && error != null
    // Se recuerda el último mensaje para que no desaparezca de golpe mientras se anima la salida.
    val ultimoError = remember { mutableStateOf("") }
    if (error != null) ultimoError.value = error

    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = hayError,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Error",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(text = ultimoError.value)
            }
        }
        if (!hayError && ayuda != null) {
            Text(text = ayuda)
        }
        if (contador != null) {
            Text(
                text = "${contador.first}/${contador.second}",
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/** Ícono de estado al final del campo: ✓ cuando el valor es correcto. */
@Composable
private fun IconoEstado(valido: Boolean) {
    Crossfade(targetState = valido, label = "iconoEstado") { ok ->
        if (ok) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Campo correcto",
                tint = colorExito()
            )
        }
    }
}

/**
 * Campo de texto con validación visual: ícono inicial, borde rojo + ícono + mensaje cuando hay error,
 * y ✓ verde cuando está correcto. No contiene reglas: recibe el error ya calculado por el ViewModel.
 */
@Composable
fun CampoValidado(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    iconoInicial: ImageVector,
    error: String?,
    mostrarError: Boolean,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.None,
    imeAction: ImeAction = ImeAction.Next,
    ayuda: String? = null,
    esClave: Boolean = false,
    claveVisible: Boolean = false,
    onAlternarClave: (() -> Unit)? = null,
    lineas: Int = 1,
    maxCaracteres: Int? = null,
    enabled: Boolean = true,
    onPerderFoco: () -> Unit = {},
    onListo: (() -> Unit)? = null
) {
    val hayError = mostrarError && error != null
    val valido = mostrarError && error == null && valor.isNotEmpty()
    var tuvoFoco by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { estado ->
                if (tuvoFoco && !estado.isFocused) onPerderFoco()
                if (estado.isFocused) tuvoFoco = true
            },
        enabled = enabled,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(iconoInicial, contentDescription = null) },
        trailingIcon = {
            if (esClave && onAlternarClave != null) {
                IconButton(onClick = onAlternarClave) {
                    Icon(
                        imageVector = if (claveVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (claveVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            } else {
                IconoEstado(valido)
            }
        },
        isError = hayError,
        singleLine = lineas == 1,
        minLines = lineas,
        maxLines = if (lineas == 1) 1 else lineas + 3,
        visualTransformation = if (esClave && !claveVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (esClave) KeyboardType.Password else teclado,
            capitalization = capitalizacion,
            imeAction = imeAction
        ),
        keyboardActions = if (onListo != null) KeyboardActions(onDone = { onListo() }) else KeyboardActions.Default,
        supportingText = {
            ApoyoCampo(
                error = error,
                mostrarError = mostrarError,
                ayuda = ayuda,
                contador = if (maxCaracteres != null) valor.length to maxCaracteres else null
            )
        }
    )
}

/** Campo con filtro dinámico: al escribir se filtran las comunas que coinciden (bonus track). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoComuna(
    valor: String,
    onValorChange: (String) -> Unit,
    error: String?,
    mostrarError: Boolean,
    onPerderFoco: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    val sugerencias = remember(valor) { Filtros.comunas(valor) }
    val menuAbierto = expandido && sugerencias.isNotEmpty()
    val hayError = mostrarError && error != null
    val valido = mostrarError && error == null && valor.isNotEmpty()
    var tuvoFoco by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = menuAbierto,
        onExpandedChange = { expandido = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {
                onValorChange(it)
                expandido = true
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .onFocusChanged { estado ->
                    if (tuvoFoco && !estado.isFocused) onPerderFoco()
                    if (estado.isFocused) tuvoFoco = true
                },
            label = { Text("Comuna") },
            leadingIcon = { Icon(Icons.Filled.Place, contentDescription = null) },
            trailingIcon = {
                if (valido) IconoEstado(true) else ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto)
            },
            isError = hayError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            supportingText = {
                ApoyoCampo(
                    error = error,
                    mostrarError = mostrarError,
                    ayuda = "Escribe para filtrar la lista"
                )
            }
        )
        ExposedDropdownMenu(
            expanded = menuAbierto,
            onDismissRequest = { expandido = false }
        ) {
            sugerencias.forEach { comuna ->
                DropdownMenuItem(
                    text = { Text(comuna) },
                    onClick = {
                        onValorChange(comuna)
                        expandido = false
                    }
                )
            }
        }
    }
}

/** Lista desplegable para elegir la modalidad de colaboración. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorTipo(
    seleccionado: TipoColaboracion?,
    onSeleccionar: (TipoColaboracion) -> Unit,
    error: String?,
    mostrarError: Boolean,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    val hayError = mostrarError && error != null

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = seleccionado?.etiqueta ?: "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text("¿Cómo quieres colaborar?") },
            leadingIcon = {
                Icon(
                    imageVector = (seleccionado ?: TipoColaboracion.DONACION).icono(),
                    contentDescription = null
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            isError = hayError,
            supportingText = {
                ApoyoCampo(
                    error = error,
                    mostrarError = mostrarError,
                    ayuda = seleccionado?.descripcion
                )
            }
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            TipoColaboracion.entries.forEach { tipo ->
                DropdownMenuItem(
                    text = { Text(tipo.etiqueta) },
                    leadingIcon = { Icon(tipo.icono(), contentDescription = null) },
                    onClick = {
                        onSeleccionar(tipo)
                        expandido = false
                    }
                )
            }
        }
    }
}

/** Campo de fecha (solo lectura) que abre un selector de calendario. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    fecha: LocalDate?,
    onFecha: (LocalDate) -> Unit,
    error: String?,
    mostrarError: Boolean,
    modifier: Modifier = Modifier
) {
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }
    val interaccion = remember { MutableInteractionSource() }
    val hayError = mostrarError && error != null
    val valido = mostrarError && error == null && fecha != null

    // Un toque en cualquier parte del campo abre el calendario.
    LaunchedEffect(interaccion) {
        interaccion.interactions.collect { evento ->
            if (evento is PressInteraction.Release) mostrarCalendario = true
        }
    }

    OutlinedTextField(
        value = fecha?.formatoCorto() ?: "",
        onValueChange = {},
        readOnly = true,
        modifier = modifier.fillMaxWidth(),
        interactionSource = interaccion,
        label = { Text("Fecha de contacto preferida") },
        placeholder = { Text("dd/mm/aaaa") },
        leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
        trailingIcon = {
            if (valido) IconoEstado(true) else {
                IconButton(onClick = { mostrarCalendario = true }) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Abrir calendario")
                }
            }
        },
        isError = hayError,
        singleLine = true,
        supportingText = {
            ApoyoCampo(
                error = error,
                mostrarError = mostrarError,
                ayuda = "Día en que prefieres que te contactemos"
            )
        }
    )

    if (mostrarCalendario) {
        val hoyUtc = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val estadoCalendario = rememberDatePickerState(
            initialSelectedDateMillis = fecha?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= hoyUtc
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoCalendario.selectedDateMillis?.let { millis ->
                        onFecha(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    mostrarCalendario = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoCalendario)
        }
    }
}

/** Casilla de verificación con mensaje de error (toda la fila es tocable). */
@Composable
fun CasillaValidada(
    marcada: Boolean,
    onCambio: (Boolean) -> Unit,
    texto: String,
    error: String?,
    mostrarError: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = marcada, role = Role.Checkbox, onValueChange = onCambio)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = marcada, onCheckedChange = null)
            Spacer(Modifier.width(12.dp))
            Text(text = texto, style = MaterialTheme.typography.bodyMedium)
        }
        AnimatedVisibility(
            visible = mostrarError && error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier.padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/** Fila con interruptor (Switch) y descripción. */
@Composable
fun FilaInterruptor(
    titulo: String,
    descripcion: String?,
    activo: Boolean,
    onCambio: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = activo, role = Role.Switch, onValueChange = onCambio)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, style = MaterialTheme.typography.bodyLarge)
            if (descripcion != null) {
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = activo, onCheckedChange = null)
    }
}
