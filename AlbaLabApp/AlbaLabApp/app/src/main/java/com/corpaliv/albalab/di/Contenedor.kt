package com.corpaliv.albalab.di

import android.content.Context
import com.corpaliv.albalab.data.local.AppDatabase
import com.corpaliv.albalab.data.preferences.SesionDataStore
import com.corpaliv.albalab.data.repository.AlmacenFotos
import com.corpaliv.albalab.data.repository.InteresRepository
import com.corpaliv.albalab.data.repository.UsuarioRepository

/** Contenedor manual de dependencias: crea una sola vez los objetos compartidos de la app. */
class Contenedor(context: Context) {
    private val appContext = context.applicationContext
    private val baseDatos by lazy { AppDatabase.obtener(appContext) }

    val sesionDataStore by lazy { SesionDataStore(appContext) }
    val usuarioRepository by lazy { UsuarioRepository(baseDatos.usuarioDao()) }
    val interesRepository by lazy { InteresRepository(baseDatos.registroInteresDao()) }
    val almacenFotos by lazy { AlmacenFotos(appContext) }
}
