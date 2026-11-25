package com.corpaliv.albalab.domain.model

/** Estados de una solicitud según la propuesta técnica (el backend aplica las mismas transiciones). */
enum class EstadoSolicitud(val etiqueta: String, val descripcion: String) {
    RECIBIDA("Recibida", "Tu solicitud llegó al equipo de vinculación."),
    EN_REVISION("En revisión", "El equipo está revisando tu solicitud."),
    CONTACTADO("Contactado", "El equipo ya se comunicó contigo."),
    CONCRETADO("Concretado", "La colaboración se concretó. ¡Gracias!"),
    DESCARTADO("Descartado", "La solicitud no pudo continuar.");

    /** Estados a los que puede pasar un gestor desde éste. Debe coincidir con ms-vinculacion. */
    fun siguientes(): List<EstadoSolicitud> = when (this) {
        RECIBIDA -> listOf(EN_REVISION, DESCARTADO)
        EN_REVISION -> listOf(CONTACTADO, DESCARTADO)
        CONTACTADO -> listOf(CONCRETADO, DESCARTADO)
        CONCRETADO, DESCARTADO -> emptyList()
    }

    companion object {
        fun desdeNombre(nombre: String?): EstadoSolicitud? = entries.firstOrNull { it.name == nombre }
    }
}

/** Solicitud tal como la ve el equipo de vinculación (incluye datos de contacto). */
data class SolicitudGestion(
    val id: Long,
    val codigo: String,
    val nombreContacto: String,
    val organizacion: String?,
    val email: String?,
    val telefono: String?,
    val tipo: TipoColaboracion?,
    val programaId: Long?,
    val productoServicioId: Long?,
    val montoClp: Int?,
    val mensaje: String?,
    val estado: EstadoSolicitud,
    val fechaCreacion: String,
    val fechaActualizacion: String
)

data class CambioEstado(
    val estadoAnterior: EstadoSolicitud?,
    val estadoNuevo: EstadoSolicitud,
    val observacion: String?,
    val usuario: String?,
    val fecha: String
)

/** Resultado público de consultar una solicitud por su código. */
data class EstadoPublico(
    val codigo: String,
    val estado: EstadoSolicitud,
    val fechaCreacion: String,
    val fechaActualizacion: String
)

/** Sesión del equipo de vinculación (JWT). Vive solo en memoria: al cerrar la app hay que volver a entrar. */
data class SesionGestor(
    val token: String,
    val rol: String,
    val nombre: String
)
