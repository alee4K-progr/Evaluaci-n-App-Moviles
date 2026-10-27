package com.corpaliv.albalab

import android.app.Application
import com.corpaliv.albalab.di.Contenedor
import com.corpaliv.albalab.nativo.NotificadorLocal

class AlbaLabApp : Application() {
    lateinit var contenedor: Contenedor
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = Contenedor(this)
        NotificadorLocal.crearCanal(this)
    }
}
