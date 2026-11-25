package com.corpaliv.albalab.domain.model

/** Dólar observado en pesos chilenos, obtenido de la API externa de indicadores. */
data class TipoCambio(
    val clpPorDolar: Double,
    /** Fecha tal como la entrega la API (ISO-8601), si viene. */
    val fecha: String? = null
)
