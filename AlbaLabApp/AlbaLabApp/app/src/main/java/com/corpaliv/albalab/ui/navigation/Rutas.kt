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
    const val GESTOR_LOGIN = "gestor_login"
    const val GESTION = "gestion"
    const val ARG_ID = "id"
    const val GESTION_DETALLE = "gestion_detalle"
    const val GESTION_DETALLE_PATRON = "$GESTION_DETALLE/{$ARG_ID}"

    fun gestionDetalle(id: Long) = "$GESTION_DETALLE/$id"

    const val ARG_TIPO = "tipo"
    const val ARG_REF = "ref"
    const val ARG_PROGRAMA = "prog"
    const val ARG_PRODUCTO = "prod"
    const val COLABORAR_PATRON =
        "$COLABORAR?$ARG_TIPO={$ARG_TIPO}&$ARG_REF={$ARG_REF}&$ARG_PROGRAMA={$ARG_PROGRAMA}&$ARG_PRODUCTO={$ARG_PRODUCTO}"

    /** Ruta del formulario de colaboración con datos precargados opcionales. */
    fun colaborar(
        tipo: TipoColaboracion? = null,
        referencia: String = "",
        programaId: String? = null,
        productoId: String? = null
    ): String {
        val params = buildList {
            if (tipo != null) add("$ARG_TIPO=${tipo.name}")
            if (referencia.isNotBlank()) add("$ARG_REF=${Uri.encode(referencia)}")
            if (!programaId.isNullOrBlank()) add("$ARG_PROGRAMA=${Uri.encode(programaId)}")
            if (!productoId.isNullOrBlank()) add("$ARG_PRODUCTO=${Uri.encode(productoId)}")
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
        GESTOR_LOGIN -> "Equipo de vinculación"
        GESTION -> "Gestión de solicitudes"
        GESTION_DETALLE_PATRON, GESTION_DETALLE -> "Detalle de solicitud"
        else -> "Alba Lab"
    }
}
