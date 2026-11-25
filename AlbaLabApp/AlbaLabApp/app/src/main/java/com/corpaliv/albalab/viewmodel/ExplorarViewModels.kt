package com.corpaliv.albalab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corpaliv.albalab.data.catalogo.CatalogoDatos
import com.corpaliv.albalab.data.repository.CatalogoRepository
import com.corpaliv.albalab.data.repository.Origen
import com.corpaliv.albalab.domain.Filtros
import com.corpaliv.albalab.domain.model.CasoImpacto
import com.corpaliv.albalab.domain.model.CategoriaPrograma
import com.corpaliv.albalab.domain.model.CategoriaProducto
import com.corpaliv.albalab.domain.model.DestinoAporte
import com.corpaliv.albalab.domain.model.Indicador
import com.corpaliv.albalab.domain.model.Producto
import com.corpaliv.albalab.domain.model.Programa
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Búsqueda y filtro dinámico de programas (se actualiza mientras se escribe).
 * La lista base viene del microservicio; mientras carga (o sin conexión) se muestra la copia local.
 */
class ProgramasViewModel(private val repositorio: CatalogoRepository) : ViewModel() {
    private val _consulta = MutableStateFlow("")
    val consulta: StateFlow<String> = _consulta

    private val _categoria = MutableStateFlow<CategoriaPrograma?>(null)
    val categoria: StateFlow<CategoriaPrograma?> = _categoria

    private val _fuente = MutableStateFlow(CatalogoDatos.programas)
    private val _origen = MutableStateFlow(Origen.LOCAL)
    val origen: StateFlow<Origen> = _origen

    val programas: StateFlow<List<Programa>> = combine(_consulta, _categoria, _fuente) { q, c, lista ->
        Filtros.programas(lista, q, c)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CatalogoDatos.programas)

    init {
        viewModelScope.launch {
            val r = repositorio.programas()
            _fuente.value = r.datos
            _origen.value = r.origen
        }
    }

    fun cambiarConsulta(v: String) { _consulta.value = v }
    fun cambiarCategoria(c: CategoriaPrograma?) { _categoria.value = c }
}

/** Búsqueda y filtro dinámico del catálogo de productos, servicios e iniciativas RSE. */
class CatalogoViewModel(private val repositorio: CatalogoRepository) : ViewModel() {
    private val _consulta = MutableStateFlow("")
    val consulta: StateFlow<String> = _consulta

    private val _categoria = MutableStateFlow<CategoriaProducto?>(null)
    val categoria: StateFlow<CategoriaProducto?> = _categoria

    private val _fuente = MutableStateFlow(CatalogoDatos.productos)
    private val _origen = MutableStateFlow(Origen.LOCAL)
    val origen: StateFlow<Origen> = _origen

    val productos: StateFlow<List<Producto>> = combine(_consulta, _categoria, _fuente) { q, c, lista ->
        Filtros.productos(lista, q, c)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CatalogoDatos.productos)

    init {
        viewModelScope.launch {
            val r = repositorio.productos()
            _fuente.value = r.datos
            _origen.value = r.origen
        }
    }

    fun cambiarConsulta(v: String) { _consulta.value = v }
    fun cambiarCategoria(c: CategoriaProducto?) { _categoria.value = c }
}

/** Contenido de las pantallas Inicio e Impacto: indicadores, historias y destino de los aportes. */
data class ContenidoUiState(
    val indicadores: List<Indicador> = CatalogoDatos.indicadores,
    val casos: List<CasoImpacto> = CatalogoDatos.casos,
    val destinoAportes: List<DestinoAporte> = CatalogoDatos.destinoAportes,
    val origen: Origen = Origen.LOCAL,
    val cargando: Boolean = true
)

class ContenidoViewModel(private val repositorio: CatalogoRepository) : ViewModel() {
    private val _estado = MutableStateFlow(ContenidoUiState())
    val estado: StateFlow<ContenidoUiState> = _estado

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            // Las tres consultas se hacen en paralelo.
            val (indicadores, casos, destino) = coroutineScope {
                val i = async { repositorio.indicadores() }
                val c = async { repositorio.casosImpacto() }
                val d = async { repositorio.destinoAportes() }
                Triple(i.await(), c.await(), d.await())
            }
            val todoRemoto = listOf(indicadores.origen, casos.origen, destino.origen).all { it == Origen.SERVIDOR }
            _estado.value = ContenidoUiState(
                indicadores = indicadores.datos,
                casos = casos.datos,
                destinoAportes = destino.datos,
                origen = if (todoRemoto) Origen.SERVIDOR else Origen.LOCAL,
                cargando = false
            )
        }
    }
}
