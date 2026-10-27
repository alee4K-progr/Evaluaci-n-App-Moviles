package com.corpaliv.albalab.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.corpaliv.albalab.data.catalogo.CatalogoDatos
import com.corpaliv.albalab.domain.model.CasoImpacto
import com.corpaliv.albalab.domain.model.CategoriaProducto
import com.corpaliv.albalab.domain.model.CategoriaPrograma
import com.corpaliv.albalab.domain.model.Producto
import com.corpaliv.albalab.domain.model.Programa
import com.corpaliv.albalab.domain.model.TipoColaboracion
import com.corpaliv.albalab.domain.validation.Validadores
import com.corpaliv.albalab.ui.components.AvisoDatosFicticios
import com.corpaliv.albalab.ui.components.BotonSecundario
import com.corpaliv.albalab.ui.components.NumeroAnimado
import com.corpaliv.albalab.ui.components.TarjetaAnimada
import com.corpaliv.albalab.ui.components.TituloSeccion
import com.corpaliv.albalab.viewmodel.CatalogoViewModel
import com.corpaliv.albalab.viewmodel.Fabrica
import com.corpaliv.albalab.viewmodel.ProgramasViewModel

/** Campo de búsqueda: filtra la lista mientras se escribe. */
@Composable
private fun CampoBusqueda(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(onClick = { onCambio("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Borrar búsqueda")
                }
            }
        },
        singleLine = true
    )
}

@Composable
private fun SinResultados(texto: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = texto,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ------------------------------------------------------------------ PROGRAMAS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramasScreen(
    onColaborar: (TipoColaboracion, String) -> Unit,
    vm: ProgramasViewModel = viewModel(factory = Fabrica.instancia)
) {
    val consulta by vm.consulta.collectAsStateWithLifecycle()
    val categoria by vm.categoria.collectAsStateWithLifecycle()
    val programas by vm.programas.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 720.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TituloSeccion(
                    titulo = "Programas de CORPALIV / Alba Lab",
                    subtitulo = "Conoce cómo se genera inclusión social y laboral"
                )
            }
            item { CampoBusqueda(consulta, vm::cambiarConsulta, "Buscar programa") }
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = categoria == null,
                        onClick = { vm.cambiarCategoria(null) },
                        label = { Text("Todos") }
                    )
                    CategoriaPrograma.entries.forEach { c ->
                        FilterChip(
                            selected = categoria == c,
                            onClick = { vm.cambiarCategoria(if (categoria == c) null else c) },
                            label = { Text(c.etiqueta) }
                        )
                    }
                }
            }
            if (programas.isEmpty()) {
                item { SinResultados("No encontramos programas con esa búsqueda. Prueba con otra palabra.") }
            }
            items(programas, key = { it.id }) { programa ->
                TarjetaPrograma(programa, onColaborar)
            }
            item { AvisoDatosFicticios() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaPrograma(
    programa: Programa,
    onColaborar: (TipoColaboracion, String) -> Unit
) {
    var expandido by rememberSaveable(programa.id) { mutableStateOf(false) }
    TarjetaAnimada {
        AssistChip(onClick = {}, label = { Text(programa.categoria.etiqueta) })
        Text(text = programa.titulo, style = MaterialTheme.typography.titleLarge)
        Text(
            text = programa.resumen,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AnimatedVisibility(visible = expandido) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = programa.detalle, style = MaterialTheme.typography.bodyMedium)
                BotonSecundario(
                    texto = "Quiero apoyar este programa",
                    icono = Icons.Filled.VolunteerActivism,
                    onClick = { onColaborar(programa.tipoSugerido, programa.titulo) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            androidx.compose.material3.TextButton(onClick = { expandido = !expandido }) {
                Text(if (expandido) "Ver menos" else "Ver más")
                Icon(
                    imageVector = if (expandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }
        }
    }
}

// ------------------------------------------------------------------ CATÁLOGO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    onColaborar: (TipoColaboracion, String) -> Unit,
    vm: CatalogoViewModel = viewModel(factory = Fabrica.instancia)
) {
    val consulta by vm.consulta.collectAsStateWithLifecycle()
    val categoria by vm.categoria.collectAsStateWithLifecycle()
    val productos by vm.productos.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 720.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TituloSeccion(
                    titulo = "Productos, servicios e iniciativas",
                    subtitulo = "Cada compra o aporte tiene un destino social claro"
                )
            }
            item { CampoBusqueda(consulta, vm::cambiarConsulta, "Buscar producto o servicio") }
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = categoria == null,
                        onClick = { vm.cambiarCategoria(null) },
                        label = { Text("Todos") }
                    )
                    CategoriaProducto.entries.forEach { c ->
                        FilterChip(
                            selected = categoria == c,
                            onClick = { vm.cambiarCategoria(if (categoria == c) null else c) },
                            label = { Text(c.etiqueta) }
                        )
                    }
                }
            }
            if (productos.isEmpty()) {
                item { SinResultados("No encontramos productos con esa búsqueda.") }
            }
            items(productos, key = { it.id }) { producto ->
                TarjetaProducto(producto, onColaborar)
            }
            item { AvisoDatosFicticios() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaProducto(
    producto: Producto,
    onColaborar: (TipoColaboracion, String) -> Unit
) {
    TarjetaAnimada {
        AssistChip(onClick = {}, label = { Text(producto.categoria.etiqueta.removeSuffix("s")) })
        Text(text = producto.nombre, style = MaterialTheme.typography.titleLarge)
        Text(
            text = producto.descripcion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = producto.precioReferencial?.let { "Valor referencial: $${Validadores.formatoMiles(it)}" }
                ?: "Valor a convenir según tu necesidad",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Destino del aporte: ${producto.destinoAporte}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val tipo = when (producto.categoria) {
            CategoriaProducto.PRODUCTO -> TipoColaboracion.COMPRA
            CategoriaProducto.SERVICIO -> TipoColaboracion.CAPACITACION
            CategoriaProducto.RSE -> TipoColaboracion.ALIANZA
        }
        BotonSecundario(
            texto = "Me interesa",
            icono = Icons.Filled.VolunteerActivism,
            onClick = { onColaborar(tipo, producto.nombre) }
        )
    }
}

// ------------------------------------------------------------------ IMPACTO

@Composable
fun ImpactoScreen(onColaborar: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 720.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TituloSeccion(
                    titulo = "Historias de impacto",
                    subtitulo = "Casos anonimizados que muestran qué cambia cuando colaboras"
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CatalogoDatos.indicadores.forEach { indicador ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            NumeroAnimado(
                                valor = indicador.valor,
                                sufijo = indicador.sufijo,
                                estilo = MaterialTheme.typography.headlineMedium
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
            items(CatalogoDatos.casos, key = { it.id }) { caso -> TarjetaCaso(caso) }
            item {
                BotonSecundario(
                    texto = "Quiero ser parte de la próxima historia",
                    icono = Icons.Filled.VolunteerActivism,
                    onClick = onColaborar
                )
            }
            item { AvisoDatosFicticios(texto = "Los casos son ficticios y anonimizados: no corresponden a personas reales.") }
        }
    }
}

@Composable
private fun TarjetaCaso(caso: CasoImpacto) {
    var expandido by rememberSaveable(caso.id) { mutableStateOf(false) }
    TarjetaAnimada {
        Text(
            text = caso.protagonista,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(text = caso.titulo, style = MaterialTheme.typography.titleLarge)
        AnimatedVisibility(visible = expandido) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = caso.historia, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Resultado: ${caso.resultado}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            androidx.compose.material3.TextButton(onClick = { expandido = !expandido }) {
                Text(if (expandido) "Ocultar historia" else "Leer historia")
                Icon(
                    imageVector = if (expandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }
        }
    }
}
