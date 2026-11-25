package com.corpaliv.albalab.domain

import com.corpaliv.albalab.domain.model.EstadoSolicitud

/** Reglas de la gestión de solicitudes que se validan en la app antes de llamar al servidor. */
object ReglasGestion {
    const val LARGO_MAXIMO_OBSERVACION = 500

    /** Descartar una solicitud exige dejar una observación; ninguna observación puede pasar de 500 caracteres. */
    fun errorObservacion(nuevo: EstadoSolicitud?, observacion: String): String? = when {
        nuevo == null -> "Selecciona el nuevo estado"
        nuevo == EstadoSolicitud.DESCARTADO && observacion.isBlank() -> "Indica por qué se descarta la solicitud"
        observacion.length > LARGO_MAXIMO_OBSERVACION -> "Máximo $LARGO_MAXIMO_OBSERVACION caracteres"
        else -> null
    }

    /** Código público de una solicitud: AL- seguido de 8 caracteres (sin 0, 1, I, O). */
    private val FORMATO_CODIGO = Regex("^AL-[A-HJ-NP-Z2-9]{8}$")

    fun errorCodigo(codigo: String): String? = when {
        codigo.isBlank() -> "Ingresa el código de tu solicitud"
        !FORMATO_CODIGO.matches(codigo.trim().uppercase()) -> "El código tiene el formato AL-XXXXXXXX"
        else -> null
    }
}
