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
            InteresViewModel(c.interesRepository, c.sesionDataStore, c.indicadoresRepository)
        }
        initializer {
            val c = app().contenedor
            PerfilViewModel(c.usuarioRepository, c.almacenFotos, c.sesionDataStore)
        }
        initializer {
            RegistrosViewModel(app().contenedor.interesRepository)
        }
        initializer { ProgramasViewModel(app().contenedor.catalogoRepository) }
        initializer { CatalogoViewModel(app().contenedor.catalogoRepository) }
        initializer { ContenidoViewModel(app().contenedor.catalogoRepository) }
        initializer { GestorLoginViewModel(app().contenedor.gestionRepository) }
        initializer { GestionViewModel(app().contenedor.gestionRepository) }
        initializer { SolicitudDetalleViewModel(app().contenedor.gestionRepository) }
    }
}
