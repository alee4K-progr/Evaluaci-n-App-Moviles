package com.corpaliv.albalab.domain

import java.util.Locale

/** Conversión de montos (lógica pura, probada con tests unitarios). */
object Conversion {

    /** Pesos chilenos a dólares. Devuelve null si el monto o el tipo de cambio no son positivos. */
    fun clpADolares(montoClp: Int, clpPorDolar: Double): Double? =
        if (montoClp <= 0 || clpPorDolar <= 0.0 || clpPorDolar.isNaN()) null else montoClp / clpPorDolar

    /** Formato con dos decimales; por defecto usa la convención chilena (coma decimal). */
    fun formatoDolares(valor: Double, locale: Locale = Locale.forLanguageTag("es-CL")): String =
        String.format(locale, "%,.2f", valor)
}
