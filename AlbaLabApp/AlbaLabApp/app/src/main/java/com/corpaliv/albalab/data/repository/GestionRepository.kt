package com.corpaliv.albalab.data.repository

import com.corpaliv.albalab.data.remote.AuthApi
import com.corpaliv.albalab.data.remote.Resultado
import com.corpaliv.albalab.data.remote.TipoError
import com.corpaliv.albalab.data.remote.VinculacionApi
import com.corpaliv.albalab.data.remote.aDominio
import com.corpaliv.albalab.data.remote.dto.CambiarEstadoRequestDto
import com.corpaliv.albalab.data.remote.dto.LoginRequestDto
import com.corpaliv.albalab.data.remote.llamarApi
import com.corpaliv.albalab.domain.model.CambioEstado
import com.corpaliv.albalab.domain.model.EstadoSolicitud
import com.corpaliv.albalab.domain.model.SesionGestor
import com.corpaliv.albalab.domain.model.SolicitudGestion

/**
 * Operaciones del equipo de vinculación: iniciar sesión (ms-admin) y administrar solicitudes (ms-vinculacion).
 * Si el servidor responde 401 se cierra la sesión local para que la pantalla pida iniciar sesión otra vez.
 */
class GestionRepository(
    private val auth: AuthApi,
    private val api: VinculacionApi,
    private val almacen: AlmacenSesionGestor
) {
    val sesion get() = almacen.sesion

    suspend fun login(email: String, clave: String): Resultado<SesionGestor> {
        return when (val r = llamarApi { auth.login(LoginRequestDto(email.trim(), clave)) }) {
            is Resultado.Ok -> {
                val token = r.valor.token
                val rol = r.valor.rol
                if (token.isNullOrBlank() || rol.isNullOrBlank()) {
                    Resultado.Fallo(TipoError.DESCONOCIDO, "La respuesta del servidor no es válida")
                } else {
                    val sesion = SesionGestor(token, rol, r.valor.nombre.orEmpty())
                    almacen.guardar(sesion)
                    Resultado.Ok(sesion)
                }
            }
            is Resultado.Fallo ->
                if (r.tipo == TipoError.NO_AUTORIZADO) Resultado.Fallo(TipoError.NO_AUTORIZADO, "Correo o contraseña incorrectos")
                else r
        }
    }

    fun cerrarSesion() = almacen.cerrar()

    suspend fun listar(estado: EstadoSolicitud?): Resultado<List<SolicitudGestion>> =
        protegido { api.listar(estado?.name).mapNotNull { it.aDominio() } }

    suspend fun detalle(id: Long): Resultado<SolicitudGestion> = convertir(protegido { api.detalle(id) }) { it.aDominio() }

    suspend fun historial(id: Long): Resultado<List<CambioEstado>> =
        protegido { api.historial(id).mapNotNull { it.aDominio() } }

    suspend fun cambiarEstado(id: Long, nuevo: EstadoSolicitud, observacion: String?): Resultado<SolicitudGestion> =
        convertir(protegido { api.cambiarEstado(id, CambiarEstadoRequestDto(nuevo.name, observacion?.trim()?.ifEmpty { null })) }) {
            it.aDominio()
        }

    suspend fun eliminar(id: Long): Resultado<Unit> = protegido { api.eliminar(id) }

    private suspend fun <T> protegido(bloque: suspend () -> T): Resultado<T> {
        val r = llamarApi(bloque)
        if (r is Resultado.Fallo && r.tipo == TipoError.NO_AUTORIZADO) almacen.cerrar()
        return r
    }

    private fun <D, T> convertir(r: Resultado<D>, mapa: (D) -> T?): Resultado<T> = when (r) {
        is Resultado.Ok -> mapa(r.valor)?.let { Resultado.Ok(it) }
            ?: Resultado.Fallo(TipoError.DESCONOCIDO, "La respuesta del servidor no es válida")
        is Resultado.Fallo -> r
    }
}
