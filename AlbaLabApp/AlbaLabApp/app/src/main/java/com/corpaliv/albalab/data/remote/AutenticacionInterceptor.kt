package com.corpaliv.albalab.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Agrega `Authorization: Bearer <token>` solo a las llamadas marcadas con [CABECERA_REQUIERE_SESION].
 * Las llamadas públicas nunca llevan el token: el gateway rechaza con 401 un token vencido incluso en rutas públicas.
 */
class AutenticacionInterceptor(private val token: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.header(CABECERA_REQUIERE_SESION) == null) return chain.proceed(original)
        val builder = original.newBuilder().removeHeader(CABECERA_REQUIERE_SESION)
        token()?.let { builder.header("Authorization", "Bearer $it") }
        return chain.proceed(builder.build())
    }
}
