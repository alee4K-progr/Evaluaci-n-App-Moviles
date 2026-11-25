package com.corpaliv.albalab

import com.corpaliv.albalab.data.local.RegistroInteresDao
import com.corpaliv.albalab.data.local.RegistroInteresEntity
import com.corpaliv.albalab.data.local.UsuarioDao
import com.corpaliv.albalab.data.local.UsuarioEntity
import com.corpaliv.albalab.data.preferences.DatosSesion
import com.corpaliv.albalab.data.preferences.PreferenciasSesion
import com.corpaliv.albalab.data.remote.AuthApi
import com.corpaliv.albalab.data.remote.ContenidosApi
import com.corpaliv.albalab.data.remote.VinculacionApi
import com.corpaliv.albalab.data.remote.dto.CambiarEstadoRequestDto
import com.corpaliv.albalab.data.remote.dto.DestinoAporteDto
import com.corpaliv.albalab.data.remote.dto.EstadoPublicoDto
import com.corpaliv.albalab.data.remote.dto.HistoriaDto
import com.corpaliv.albalab.data.remote.dto.HistorialDto
import com.corpaliv.albalab.data.remote.dto.IndicadorDto
import com.corpaliv.albalab.data.remote.dto.LoginRequestDto
import com.corpaliv.albalab.data.remote.dto.LoginResponseDto
import com.corpaliv.albalab.data.remote.dto.ProductoDto
import com.corpaliv.albalab.data.remote.dto.ProgramaDto
import com.corpaliv.albalab.data.remote.dto.SolicitudDto
import com.corpaliv.albalab.data.remote.dto.SolicitudRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** DAO en memoria para probar repositorios sin Room ni Android. */
class FakeRegistroDao : RegistroInteresDao {
    private val datos = MutableStateFlow<List<RegistroInteresEntity>>(emptyList())
    private var siguienteId = 1L

    override suspend fun insertar(registro: RegistroInteresEntity): Long {
        val id = siguienteId++
        datos.value = datos.value + registro.copy(id = id)
        return id
    }

    override fun observarDe(correo: String?): Flow<List<RegistroInteresEntity>> = datos.map { registros -> registros.filter { it.usuarioCorreo == correo } }

    override suspend fun pendientes(correo: String?): List<RegistroInteresEntity> =
        datos.value.filter { it.usuarioCorreo == correo && !it.sincronizado }.sortedBy { it.creadoEn }

    override suspend fun enviados(correo: String?): List<RegistroInteresEntity> = datos.value.filter { it.usuarioCorreo == correo && it.remoteId != null }

    override suspend fun buscar(id: Long): RegistroInteresEntity? = datos.value.firstOrNull { it.id == id }

    override suspend fun marcarSincronizado(id: Long, remoteId: String?, estado: String?) {
        datos.value = datos.value.map {
            if (it.id == id) it.copy(sincronizado = true, remoteId = remoteId, estadoRemoto = estado) else it
        }
    }

    override suspend fun actualizarEstado(id: Long, estado: String?) {
        datos.value = datos.value.map { if (it.id == id) it.copy(estadoRemoto = estado) else it }
    }

    override suspend fun actualizar(registro: RegistroInteresEntity) {
        datos.value = datos.value.map { if (it.id == registro.id) registro else it }
    }

    override suspend fun eliminar(id: Long, correo: String?) {
        datos.value = datos.value.filterNot { it.id == id && it.usuarioCorreo == correo }
    }

    fun todos(): List<RegistroInteresEntity> = datos.value
}

/** DAO de usuarios en memoria. */
class FakeUsuarioDao : UsuarioDao {
    private val datos = MutableStateFlow<Map<String, UsuarioEntity>>(emptyMap())

    override suspend fun insertar(usuario: UsuarioEntity): Long {
        if (datos.value.containsKey(usuario.correo)) return -1L
        datos.value = datos.value + (usuario.correo to usuario)
        return datos.value.size.toLong()
    }

    override suspend fun buscar(correo: String): UsuarioEntity? = datos.value[correo]

    override fun observar(correo: String): Flow<UsuarioEntity?> = datos.map { it[correo] }

    override suspend fun actualizarFoto(correo: String, ruta: String?) {
        datos.value[correo]?.let { datos.value = datos.value + (correo to it.copy(fotoPath = ruta)) }
    }
}

/** Preferencias de sesión en memoria (sustituye a DataStore en los tests de ViewModels). */
class FakePreferencias(notificaciones: Boolean = true, invitado: Boolean = false) : PreferenciasSesion {
    private val _sesion = MutableStateFlow(DatosSesion(correo = null, invitado = invitado))
    private val _notificaciones = MutableStateFlow(notificaciones)

    override val sesion: Flow<DatosSesion> = _sesion
    override val notificacionesActivas: Flow<Boolean> = _notificaciones

    override suspend fun iniciarSesion(correo: String) { _sesion.value = DatosSesion(correo, false) }
    override suspend fun entrarComoInvitado() { _sesion.value = DatosSesion(null, true) }
    override suspend fun cerrarSesion() { _sesion.value = DatosSesion(null, false) }
    override suspend fun guardarNotificaciones(activas: Boolean) { _notificaciones.value = activas }
}

fun httpError(codigo: Int, cuerpo: String = ""): HttpException =
    HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody(null)))

/** ms-contenidos falso: cada operación devuelve datos o lanza una excepción. */
class FakeContenidosApi(
    var programas: () -> List<ProgramaDto> = { emptyList() },
    var productos: () -> List<ProductoDto> = { emptyList() },
    var historias: () -> List<HistoriaDto> = { emptyList() },
    var indicadores: () -> List<IndicadorDto> = { emptyList() },
    var destino: () -> List<DestinoAporteDto> = { emptyList() }
) : ContenidosApi {
    override suspend fun programas() = programas.invoke()
    override suspend fun productos(categoria: String?) = productos.invoke()
    override suspend fun historias() = historias.invoke()
    override suspend fun indicadores() = indicadores.invoke()
    override suspend fun destinoAportes() = destino.invoke()
}

/** ms-vinculacion falso. `crear` y `estado` pueden lanzar excepciones para simular fallos de red o del servidor. */
class FakeVinculacionApi : VinculacionApi {
    var crear: (SolicitudRequestDto) -> EstadoPublicoDto = { EstadoPublicoDto("AL-ABCD2345", "RECIBIDA", "2026-03-10T12:00:00", "2026-03-10T12:00:00") }
    var estado: (String) -> EstadoPublicoDto = { EstadoPublicoDto(it, "RECIBIDA", "2026-03-10T12:00:00", "2026-03-10T12:00:00") }
    var listar: (String?) -> List<SolicitudDto> = { emptyList() }
    var detalle: (Long) -> SolicitudDto = { solicitudDtoDePrueba(it) }
    var cambiar: (Long, CambiarEstadoRequestDto) -> SolicitudDto = { id, c -> solicitudDtoDePrueba(id).copy(estado = c.estado) }
    var historial: (Long) -> List<HistorialDto> = { emptyList() }
    var eliminar: (Long) -> Unit = {}

    val enviados = mutableListOf<SolicitudRequestDto>()
    val eliminados = mutableListOf<Long>()

    override suspend fun crear(solicitud: SolicitudRequestDto): EstadoPublicoDto {
        val r = crear.invoke(solicitud) // si lanza, no se registra como enviado
        enviados += solicitud
        return r
    }
    override suspend fun estado(codigo: String) = estado.invoke(codigo)
    override suspend fun listar(estado: String?) = listar.invoke(estado)
    override suspend fun detalle(id: Long) = detalle.invoke(id)
    override suspend fun cambiarEstado(id: Long, cambio: CambiarEstadoRequestDto) = cambiar.invoke(id, cambio)
    override suspend fun historial(id: Long) = historial.invoke(id)
    override suspend fun eliminar(id: Long) { eliminar.invoke(id); eliminados += id }

    companion object {
        fun sinRed(): () -> Nothing = { throw IOException("sin conexión") }
    }
}

class FakeAuthApi : AuthApi {
    var login: (LoginRequestDto) -> LoginResponseDto = { LoginResponseDto("token-de-prueba", "GESTOR", "Gestora", 480) }
    override suspend fun login(credenciales: LoginRequestDto) = login.invoke(credenciales)
}

fun solicitudDtoDePrueba(id: Long = 1L) = SolicitudDto(
    id = id, codigo = "AL-ABCD2345", nombreContacto = "Ana Prueba", organizacion = null,
    email = "ana@example.cl", telefono = null, tipoColaboracion = "DONACION", programaId = null,
    productoServicioId = null, montoClp = 5000, mensaje = "Hola", estado = "RECIBIDA",
    fechaCreacion = "2026-03-10T12:00:00", fechaActualizacion = "2026-03-10T12:00:00"
)

fun registroDePrueba(
    correo: String = "persona.prueba@example.cl",
    creadoEn: Long = 1_000L,
    fechaContacto: Long = 0L
) = RegistroInteresEntity(
    usuarioCorreo = null,
    nombre = "Persona de Prueba",
    correo = correo,
    telefono = "912345678",
    esEmpresa = false,
    organizacion = "",
    comuna = "Providencia",
    tipo = "VOLUNTARIADO",
    monto = null,
    fechaContacto = fechaContacto,
    mensaje = "Hola",
    aceptaTerminos = true,
    recibeNovedades = false,
    creadoEn = creadoEn
)
