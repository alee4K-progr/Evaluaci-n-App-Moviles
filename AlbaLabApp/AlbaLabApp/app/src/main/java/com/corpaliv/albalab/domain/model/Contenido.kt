package com.corpaliv.albalab.domain.model

/** Programa institucional de CORPALIV / Alba Lab. */
data class Programa(
    val id: String,
    val titulo: String,
    val resumen: String,
    val detalle: String,
    val categoria: CategoriaPrograma,
    val tipoSugerido: TipoColaboracion
)

enum class CategoriaPrograma(val etiqueta: String) {
    FORMACION("Formación"),
    EMPLEO("Inclusión laboral"),
    PRODUCCION("Producción"),
    VINCULACION("Vinculación")
}

/** Producto, servicio o iniciativa de RSE de la oferta con impacto social. */
data class Producto(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precioReferencial: Int?,
    val destinoAporte: String,
    val categoria: CategoriaProducto
)

enum class CategoriaProducto(val etiqueta: String) {
    PRODUCTO("Productos"),
    SERVICIO("Servicios"),
    RSE("Iniciativas RSE")
}

/** Historia de impacto (ficticia / anonimizada en esta versión de prueba). */
data class CasoImpacto(
    val id: String,
    val titulo: String,
    val protagonista: String,
    val historia: String,
    val resultado: String
)

/** Indicador clave de impacto (cifra ilustrativa para el MVP). */
data class Indicador(
    val etiqueta: String,
    val valor: Int,
    val sufijo: String = ""
)

/** Porción del destino de los aportes (porcentaje ilustrativo). */
data class DestinoAporte(
    val concepto: String,
    val porcentaje: Int
)
