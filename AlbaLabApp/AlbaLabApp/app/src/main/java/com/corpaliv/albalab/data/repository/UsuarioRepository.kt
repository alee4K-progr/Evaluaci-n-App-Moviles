package com.corpaliv.albalab.data.repository

import com.corpaliv.albalab.data.local.UsuarioDao
import com.corpaliv.albalab.data.local.UsuarioEntity
import com.corpaliv.albalab.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/** Resultado de las operaciones de autenticación (sin excepciones para el flujo normal). */
sealed interface ResultadoAuth {
    data class Exito(val correo: String) : ResultadoAuth
    data class Error(val mensaje: String) : ResultadoAuth
}

class UsuarioRepository(private val dao: UsuarioDao) {

    fun observar(correo: String): Flow<UsuarioEntity?> = dao.observar(correo)

    suspend fun registrar(nombre: String, correo: String, clave: String): ResultadoAuth =
        withContext(Dispatchers.Default) {
            val correoNormalizado = correo.trim().lowercase()
            val cifrada = PasswordHasher.cifrar(clave)
            val id = dao.insertar(
                UsuarioEntity(
                    correo = correoNormalizado,
                    nombre = nombre.trim(),
                    claveHash = cifrada.hash,
                    claveSal = cifrada.sal
                )
            )
            if (id == -1L) ResultadoAuth.Error("Ya existe una cuenta con ese correo")
            else ResultadoAuth.Exito(correoNormalizado)
        }

    suspend fun iniciarSesion(correo: String, clave: String): ResultadoAuth =
        withContext(Dispatchers.Default) {
            val correoNormalizado = correo.trim().lowercase()
            val usuario = dao.buscar(correoNormalizado)
            // Mismo mensaje para "no existe" y "clave incorrecta": no revela qué correos existen.
            if (usuario != null && PasswordHasher.verificar(clave, usuario.claveHash, usuario.claveSal)) {
                ResultadoAuth.Exito(usuario.correo)
            } else {
                ResultadoAuth.Error("Correo o contraseña incorrectos")
            }
        }

    suspend fun actualizarFoto(correo: String, ruta: String?) = dao.actualizarFoto(correo, ruta)
}
