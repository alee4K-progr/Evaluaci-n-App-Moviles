package com.corpaliv.albalab.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Construye los clientes Retrofit. El cliente público se comparte; el del gateway agrega el JWT cuando corresponde. */
object RedFactory {

    val cliente: OkHttpClient by lazy { construirCliente() }

    fun construirCliente(vararg interceptores: okhttp3.Interceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .apply { interceptores.forEach { addInterceptor(it) } }
            .build()

    fun <T> crear(baseUrl: String, servicio: Class<T>, http: OkHttpClient = cliente): T =
        Retrofit.Builder()
            .baseUrl(normalizar(baseUrl))
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(servicio)

    /** Retrofit exige que la URL base termine en "/". */
    internal fun normalizar(baseUrl: String): String =
        if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
}
