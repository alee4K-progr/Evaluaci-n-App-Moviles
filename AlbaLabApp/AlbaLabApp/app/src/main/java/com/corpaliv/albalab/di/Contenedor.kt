package com.corpaliv.albalab.di

import android.content.Context
import com.corpaliv.albalab.BuildConfig
import com.corpaliv.albalab.data.local.AppDatabase
import com.corpaliv.albalab.data.preferences.SesionDataStore
import com.corpaliv.albalab.data.remote.AuthApi
import com.corpaliv.albalab.data.remote.AutenticacionInterceptor
import com.corpaliv.albalab.data.remote.ContenidosApi
import com.corpaliv.albalab.data.remote.IndicadoresApi
import com.corpaliv.albalab.data.remote.RedFactory
import com.corpaliv.albalab.data.remote.VinculacionApi
import com.corpaliv.albalab.data.repository.AlmacenFotos
import com.corpaliv.albalab.data.repository.AlmacenSesionGestor
import com.corpaliv.albalab.data.repository.CatalogoRepository
import com.corpaliv.albalab.data.repository.GestionRepository
import com.corpaliv.albalab.data.repository.IndicadoresEconomicosRepository
import com.corpaliv.albalab.data.repository.InteresRepository
import com.corpaliv.albalab.data.repository.UsuarioRepository

/** Contenedor manual de dependencias: crea una sola vez los objetos compartidos de la app. */
class Contenedor(context: Context) {
    private val appContext = context.applicationContext
    private val baseDatos by lazy { AppDatabase.obtener(appContext) }

    val sesionDataStore by lazy { SesionDataStore(appContext) }
    val usuarioRepository by lazy { UsuarioRepository(baseDatos.usuarioDao()) }

    // Sesión del equipo de vinculación (JWT) solo en memoria.
    val almacenSesionGestor by lazy { AlmacenSesionGestor() }

    // Red: todos los microservicios propios (Spring Boot) se consumen a través del api-gateway.
    private val clienteGateway by lazy {
        RedFactory.construirCliente(AutenticacionInterceptor { almacenSesionGestor.token })
    }
    private val authApi by lazy { RedFactory.crear(BuildConfig.GATEWAY_BASE_URL, AuthApi::class.java, clienteGateway) }
    private val contenidosApi by lazy { RedFactory.crear(BuildConfig.GATEWAY_BASE_URL, ContenidosApi::class.java, clienteGateway) }
    private val vinculacionApi by lazy { RedFactory.crear(BuildConfig.GATEWAY_BASE_URL, VinculacionApi::class.java, clienteGateway) }
    // API pública externa (mindicador.cl): cliente sin el interceptor del JWT.
    private val indicadoresApi by lazy { RedFactory.crear(BuildConfig.INDICADORES_BASE_URL, IndicadoresApi::class.java) }

    val catalogoRepository by lazy { CatalogoRepository(contenidosApi, BuildConfig.GATEWAY_BASE_URL) }
    val indicadoresRepository by lazy { IndicadoresEconomicosRepository(indicadoresApi) }
    val interesRepository by lazy { InteresRepository(baseDatos.registroInteresDao(), vinculacionApi, sesionDataStore) }
    val gestionRepository by lazy { GestionRepository(authApi, vinculacionApi, almacenSesionGestor) }
    val almacenFotos by lazy { AlmacenFotos(appContext) }
}
