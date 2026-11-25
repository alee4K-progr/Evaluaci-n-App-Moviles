package com.corpaliv.albalab.data.remote.dto

/*
 * Objetos de transferencia (DTO) tal como viajan en el JSON del api-gateway.
 * Los campos de respuesta son anulables a propósito: Gson ignora la nulabilidad de Kotlin, así que si el
 * servidor omite un campo el mapeador (Mapeadores.kt) descarta ese elemento en vez de fallar la app.
 * Estas clases están protegidas de la ofuscación en proguard-rules.pro.
 */

// ---- ms-contenidos (/contenidos) ----

data class ProgramaDto(
    val id: Long? = null,
    val nombre: String? = null,
    val resumen: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val tipoSugerido: String? = null,
    val imagenUrl: String? = null
)

data class ProductoDto(
    val id: Long? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val precioReferencial: Int? = null,
    val destinoAporte: String? = null,
    val imagenUrl: String? = null,
    val disponible: Boolean? = null
)

data class HistoriaDto(
    val id: Long? = null,
    val titulo: String? = null,
    val protagonista: String? = null,
    val historia: String? = null,
    val resultado: String? = null
)

data class IndicadorDto(
    val id: Long? = null,
    val nombre: String? = null,
    val valor: Int? = null,
    val unidad: String? = null,
    val periodo: String? = null,
    val fuente: String? = null
)

data class DestinoAporteDto(
    val concepto: String? = null,
    val porcentaje: Int? = null
)

// ---- ms-admin (/auth) ----

data class LoginRequestDto(val email: String, val password: String)

data class LoginResponseDto(
    val token: String? = null,
    val rol: String? = null,
    val nombre: String? = null,
    val expiraEnMinutos: Long? = null
)

// ---- ms-vinculacion (/vinculacion/solicitudes) ----

data class SolicitudRequestDto(
    val nombreContacto: String,
    val organizacion: String?,
    val email: String?,
    val telefono: String?,
    val tipoColaboracion: String,
    val programaId: Long?,
    val productoServicioId: Long?,
    val montoClp: Int?,
    val mensaje: String?,
    val aceptaPrivacidad: Boolean,
    val claveIdempotencia: String? = null
)

data class EstadoPublicoDto(
    val codigo: String? = null,
    val estado: String? = null,
    val fechaCreacion: String? = null,
    val fechaActualizacion: String? = null
)

data class SolicitudDto(
    val id: Long? = null,
    val codigo: String? = null,
    val nombreContacto: String? = null,
    val organizacion: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val tipoColaboracion: String? = null,
    val programaId: Long? = null,
    val productoServicioId: Long? = null,
    val montoClp: Int? = null,
    val mensaje: String? = null,
    val estado: String? = null,
    val fechaCreacion: String? = null,
    val fechaActualizacion: String? = null
)

data class HistorialDto(
    val id: Long? = null,
    val estadoAnterior: String? = null,
    val estadoNuevo: String? = null,
    val observacionInterna: String? = null,
    val usuarioGestor: String? = null,
    val fechaCambio: String? = null
)

data class CambiarEstadoRequestDto(val estado: String, val observacion: String?)

/** Cuerpo de error del backend: {"error": "...", "mensaje": "..."}. */
data class ErrorDto(val error: String? = null, val mensaje: String? = null)

// ---- API externa: mindicador.cl (indicadores económicos de Chile) ----

data class MindicadorDto(
    val dolar: SerieIndicadorDto? = null,
    val uf: SerieIndicadorDto? = null
)

data class SerieIndicadorDto(
    val codigo: String? = null,
    val nombre: String? = null,
    val fecha: String? = null,
    val valor: Double? = null
)
