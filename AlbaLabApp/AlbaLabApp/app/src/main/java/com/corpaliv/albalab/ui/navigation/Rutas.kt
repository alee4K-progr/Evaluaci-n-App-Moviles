package com.corpaliv.albalab.ui.navigation

import android.net.Uri
import com.corpaliv.albalab.domain.model.TipoColaboracion

object Rutas {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val INICIO = "inicio"
    const val PROGRAMAS = "programas"
    const val CATALOGO = "catalogo"
    const val IMPACTO = "impacto"
    const val COLABORAR = "colaborar"
    const val PERFIL = "perfil"
    const val REGISTROS = "registros"

    const val ARG_TIPO = "tipo"
    const val ARG_REF = "ref"
    const val COLABORAR_PATRON = "$COLABORAR?$ARG_TIPO={$ARG_TIPO}&$ARG_REF={$ARG_REF}"

    /** Ruta del formulario de colaboración con datos precargados opcionales. */
    fun colaborar(tipo: TipoColaboracion? = null, referencia: String = ""): String {
        val params = buildList {
            if (tipo != null) add("$ARG_TIPO=${tipo.name}")
            if (referencia.isNotBlank()) add("$ARG_REF=${Uri.encode(referencia)}")
        }
        return if (params.isEmpty()) COLABORAR else "$COLABORAR?${params.joinToString("&")}"
    }

    /** Rutas que muestran la barra de navegación inferior. */
    val principales = setOf(INICIO, PROGRAMAS, CATALOGO, IMPACTO, COLABORAR)

    fun titulo(ruta: String?): String = when (ruta) {
        INICIO -> "Alba Lab"
        PROGRAMAS -> "Programas"
        CATALOGO -> "Productos y servicios"
        IMPACTO -> "Impacto social"
        COLABORAR -> "Quiero colaborar"
        PERFIL -> "Mi perfil"
        REGISTROS -> "Mis registros"
        else -> "Alba Lab"
    }
}
