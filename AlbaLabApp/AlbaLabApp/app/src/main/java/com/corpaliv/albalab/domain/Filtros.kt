package com.corpaliv.albalab.domain

import com.corpaliv.albalab.data.catalogo.CatalogoDatos
import com.corpaliv.albalab.domain.model.CategoriaPrograma
import com.corpaliv.albalab.domain.model.CategoriaProducto
import com.corpaliv.albalab.domain.model.Producto
import com.corpaliv.albalab.domain.model.Programa
import java.text.Normalizer

/** Quita tildes y pasa a minúsculas para comparar textos ("Ñuñoa" ≈ "nunoa"). */
fun String.normalizado(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()
        .trim()

/** Filtros dinámicos (bonus track): se aplican mientras el usuario escribe. */
object Filtros {

    fun comunas(consulta: String, limite: Int = 6): List<String> {
        val q = consulta.normalizado()
        if (q.isEmpty()) return CatalogoDatos.comunas.take(limite)
        return CatalogoDatos.comunas.filter { it.normalizado().contains(q) }.take(limite)
    }

    fun programas(
        lista: List<Programa>,
        consulta: String,
        categoria: CategoriaPrograma?
    ): List<Programa> {
        val q = consulta.normalizado()
        return lista.filter { p ->
            (categoria == null || p.categoria == categoria) &&
                (q.isEmpty() ||
                    p.titulo.normalizado().contains(q) ||
                    p.resumen.normalizado().contains(q) ||
                    p.detalle.normalizado().contains(q))
        }
    }

    fun productos(
        lista: List<Producto>,
        consulta: String,
        categoria: CategoriaProducto?
    ): List<Producto> {
        val q = consulta.normalizado()
        return lista.filter { p ->
            (categoria == null || p.categoria == categoria) &&
                (q.isEmpty() ||
                    p.nombre.normalizado().contains(q) ||
                    p.descripcion.normalizado().contains(q) ||
                    p.destinoAporte.normalizado().contains(q))
        }
    }
}
