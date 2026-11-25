package com.corpaliv.albalab.data.remote

import com.corpaliv.albalab.data.remote.dto.ErrorDto
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

enum class TipoError { SIN_CONEXION, NO_AUTORIZADO, SIN_PERMISO, NO_ENCONTRADO, CONFLICTO, INVALIDO, SERVIDOR, DESCONOCIDO }

/** Resultado de una llamada a la red: o trae el valor, o un error clasificado con un mensaje para la persona. */
sealed interface Resultado<out T> {
    data class Ok<T>(val valor: T) : Resultado<T>
    data class Fallo(val tipo: TipoError, val mensaje: String) : Resultado<Nothing>
}

private val gson = Gson()

/** Convierte excepciones de red/HTTP en [Resultado.Fallo]; relanza la cancelación de corrutinas. */
suspend fun <T> llamarApi(bloque: suspend () -> T): Resultado<T> = try {
    Resultado.Ok(bloque())
} catch (e: CancellationException) {
    throw e
} catch (e: IOException) {
    Resultado.Fallo(TipoError.SIN_CONEXION, "Sin conexión con el servidor. Revisa tu internet e inténtalo de nuevo.")
} catch (e: HttpException) {
    val tipo = when (e.code()) {
        400 -> TipoError.INVALIDO
        401 -> TipoError.NO_AUTORIZADO
        403 -> TipoError.SIN_PERMISO
        404 -> TipoError.NO_ENCONTRADO
        409 -> TipoError.CONFLICTO
        in 500..599 -> TipoError.SERVIDOR
        else -> TipoError.DESCONOCIDO
    }
    Resultado.Fallo(tipo, mensajeDelServidor(e) ?: mensajePorDefecto(tipo))
} catch (e: Exception) {
    Resultado.Fallo(TipoError.DESCONOCIDO, "Ocurrió un error inesperado.")
}

internal fun mensajeDelServidor(e: HttpException): String? = try {
    val cuerpo = e.response()?.errorBody()?.string()
    if (cuerpo.isNullOrBlank()) null else gson.fromJson(cuerpo, ErrorDto::class.java)?.mensaje?.takeIf { it.isNotBlank() }
} catch (ex: Exception) {
    null
}

private fun mensajePorDefecto(tipo: TipoError): String = when (tipo) {
    TipoError.NO_AUTORIZADO -> "Tu sesión no es válida o venció. Inicia sesión nuevamente."
    TipoError.SIN_PERMISO -> "Tu rol no permite esta acción."
    TipoError.NO_ENCONTRADO -> "No se encontró lo que buscas."
    TipoError.CONFLICTO -> "La acción no es posible en el estado actual."
    TipoError.INVALIDO -> "Los datos enviados no son válidos."
    TipoError.SERVIDOR -> "El servidor tuvo un problema. Inténtalo más tarde."
    else -> "No se pudo completar la operación."
}
