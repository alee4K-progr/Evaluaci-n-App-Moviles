package com.corpaliv.albalab

import com.corpaliv.albalab.data.catalogo.CatalogoDatos
import com.corpaliv.albalab.domain.Filtros
import com.corpaliv.albalab.domain.model.CategoriaProducto
import com.corpaliv.albalab.domain.model.CategoriaPrograma
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FiltrosTest {

    @Test
    fun comunas_se_filtran_ignorando_tildes_y_mayusculas() {
        assertTrue(Filtros.comunas("nunoa").contains("Ñuñoa"))
        assertTrue(Filtros.comunas("VIÑA").contains("Viña del Mar"))
    }

    @Test
    fun comunas_sin_coincidencias_devuelve_lista_vacia() {
        assertTrue(Filtros.comunas("zzzz").isEmpty())
    }

    @Test
    fun programas_se_filtran_por_texto_y_categoria() {
        val porTexto = Filtros.programas(CatalogoDatos.programas, "empleo", null)
        assertTrue(porTexto.isNotEmpty())
        val porCategoria = Filtros.programas(CatalogoDatos.programas, "", CategoriaPrograma.PRODUCCION)
        assertEquals(1, porCategoria.size)
    }

    @Test
    fun productos_se_filtran_por_categoria() {
        val rse = Filtros.productos(CatalogoDatos.productos, "", CategoriaProducto.RSE)
        assertTrue(rse.all { it.categoria == CategoriaProducto.RSE })
        assertTrue(rse.isNotEmpty())
    }
}
