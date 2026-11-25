package com.corpaliv.albalab.data.repository

import com.corpaliv.albalab.data.local.RegistroInteresDao
import com.corpaliv.albalab.data.local.RegistroInteresEntity
import com.corpaliv.albalab.data.preferences.PreferenciasSesion
import com.corpaliv.albalab.data.remote.Resultado
import com.corpaliv.albalab.data.remote.VinculacionApi
import com.corpaliv.albalab.data.remote.aDominio
import com.corpaliv.albalab.data.remote.aSolicitudDto
import com.corpaliv.albalab.data.remote.llamarApi
import com.corpaliv.albalab.domain.model.EstadoPublico
import com.corpaliv.albalab.domain.model.EstadoSolicitud
import com.corpaliv.albalab.data.remote.TipoError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

data class ResultadoGuardado(val id: Long, val sincronizado: Boolean)

/**
 * Patrón "primero local": cada registro se guarda en Room (nunca se pierde) y luego se intenta enviar
 * como solicitud a ms-vinculacion (vía api-gateway). Si no hay conexión queda con `sincronizado = false`
 * y se reintenta después con [sincronizarPendientes]. El servidor responde con un código (AL-XXXXXXXX)
 * con el que la persona puede consultar el estado sin iniciar sesión.
 */
class InteresRepository(
    private val dao: RegistroInteresDao,
    private val api: VinculacionApi,
    private val preferencias: PreferenciasSesion
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val registros: Flow<List<RegistroInteresEntity>> = preferencias.sesion.flatMapLatest { sesion ->
        if (sesion.correo != null || sesion.invitado) {
            dao.observarDe(sesion.correo).onStart { emit(emptyList()) }
        } else {
            flowOf(emptyList())
        }
    }

    // Evita enviar dos veces el mismo registro si dos pantallas sincronizan a la vez.
    private val candado = Mutex()

    suspend fun guardar(registro: RegistroInteresEntity): Long = dao.insertar(registro)

    suspend fun guardarYEnviar(registro: RegistroInteresEntity): ResultadoGuardado {
        val id = dao.insertar(registro)
        sincronizarPendientesDe(registro.usuarioCorreo)
        return ResultadoGuardado(id = id, sincronizado = dao.buscar(id)?.sincronizado == true)
    }

    /** Envía los registros pendientes (del más antiguo al más nuevo) y devuelve los ids enviados. */
    suspend fun sincronizarPendientes(): List<Long> {
        val sesion = preferencias.sesion.first()
        if (sesion.correo == null && !sesion.invitado) return emptyList()
        return sincronizarPendientesDe(sesion.correo)
    }

    private suspend fun sincronizarPendientesDe(correo: String?): List<Long> = candado.withLock {
        val enviados = mutableListOf<Long>()
        for (registro in dao.pendientes(correo)) {
            try {
                val respuesta = api.crear(registro.aSolicitudDto()).aDominio() ?: continue
                dao.marcarSincronizado(registro.id, respuesta.codigo, respuesta.estado.name)
                enviados += registro.id
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                break // sin conexión: no tiene sentido intentar con los demás ahora
            } catch (e: HttpException) {
                continue // el servidor rechazó este registro; queda pendiente y se sigue con los otros
            } catch (e: Exception) {
                continue
            }
        }
        enviados
    }

    /**
     * Pregunta al servidor por el estado actual de cada registro enviado que aún no esté en un estado final.
     * Devuelve cuántos registros cambiaron de estado.
     */
    suspend fun refrescarEstados(): Int {
        val sesion = preferencias.sesion.first()
        if (sesion.correo == null && !sesion.invitado) return 0
        var cambiados = 0
        for (registro in dao.enviados(sesion.correo)) {
            val codigo = registro.remoteId ?: continue
            if (EstadoSolicitud.desdeNombre(registro.estadoRemoto)?.siguientes()?.isEmpty() == true) continue
            when (val r = consultarEstado(codigo)) {
                is Resultado.Ok -> if (r.valor.estado.name != registro.estadoRemoto) {
                    dao.actualizarEstado(registro.id, r.valor.estado.name)
                    cambiados++
                }
                is Resultado.Fallo -> if (r.tipo == TipoError.SIN_CONEXION) break
            }
        }
        return cambiados
    }

    /** Consulta pública por código (no requiere iniciar sesión). */
    suspend fun consultarEstado(codigo: String): Resultado<EstadoPublico> {
        val limpio = codigo.trim().uppercase()
        if (limpio.isEmpty()) return Resultado.Fallo(TipoError.INVALIDO, "Ingresa el código de tu solicitud")
        return when (val r = llamarApi { api.estado(limpio) }) {
            is Resultado.Ok -> r.valor.aDominio()?.let { Resultado.Ok(it) }
                ?: Resultado.Fallo(TipoError.DESCONOCIDO, "La respuesta del servidor no es válida")
            is Resultado.Fallo -> r
        }
    }

    suspend fun eliminar(id: Long) {
        val sesion = preferencias.sesion.first()
        if (sesion.correo != null || sesion.invitado) {
            candado.withLock { dao.eliminar(id, sesion.correo) }
        }
    }
}
