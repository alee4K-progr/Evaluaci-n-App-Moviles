package com.corpaliv.albalab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corpaliv.albalab.data.catalogo.CatalogoDatos
import com.corpaliv.albalab.domain.Filtros
import com.corpaliv.albalab.domain.model.CategoriaPrograma
import com.corpaliv.albalab.domain.model.CategoriaProducto
import com.corpaliv.albalab.domain.model.Producto
import com.corpaliv.albalab.domain.model.Programa
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Búsqueda y filtro dinámico de programas (se actualiza mientras se escribe). */
class ProgramasViewModel : ViewModel() {
    private val _consulta = MutableStateFlow("")
    val consulta: StateFlow<String> = _consulta

    private val _categoria = MutableStateFlow<CategoriaPrograma?>(null)
    val categoria: StateFlow<CategoriaPrograma?> = _categoria

    val programas: StateFlow<List<Programa>> = combine(_consulta, _categoria) { q, c ->
        Filtros.programas(CatalogoDatos.programas, q, c)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CatalogoDatos.programas)

    fun cambiarConsulta(v: String) { _consulta.value = v }
    fun cambiarCategoria(c: CategoriaPrograma?) { _categoria.value = c }
}

/** Búsqueda y filtro dinámico del catálogo de productos, servicios e iniciativas RSE. */
class CatalogoViewModel : ViewModel() {
    private val _consulta = MutableStateFlow("")
    val consulta: StateFlow<String> = _consulta

    private val _categoria = MutableStateFlow<CategoriaProducto?>(null)
    val categoria: StateFlow<CategoriaProducto?> = _categoria

    val productos: StateFlow<List<Producto>> = combine(_consulta, _categoria) { q, c ->
        Filtros.productos(CatalogoDatos.productos, q, c)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CatalogoDatos.productos)

    fun cambiarConsulta(v: String) { _consulta.value = v }
    fun cambiarCategoria(c: CategoriaProducto?) { _categoria.value = c }
}
