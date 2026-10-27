package com.corpaliv.albalab.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.corpaliv.albalab.AlbaLabApp

private fun CreationExtras.app(): AlbaLabApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AlbaLabApp

/** Fábrica única de ViewModels: inyecta repositorios desde el contenedor de dependencias. */
object Fabrica {
    val instancia: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val c = app().contenedor
            AuthViewModel(c.usuarioRepository, c.sesionDataStore)
        }
        initializer {
            val c = app().contenedor
            InteresViewModel(c.interesRepository, c.sesionDataStore)
        }
        initializer {
            val c = app().contenedor
            PerfilViewModel(c.usuarioRepository, c.almacenFotos, c.sesionDataStore)
        }
        initializer {
            RegistrosViewModel(app().contenedor.interesRepository)
        }
        initializer { ProgramasViewModel() }
        initializer { CatalogoViewModel() }
    }
}
