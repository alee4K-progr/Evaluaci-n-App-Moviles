package com.corpaliv.albalab.data.remote

import com.corpaliv.albalab.data.remote.dto.CambiarEstadoRequestDto
import com.corpaliv.albalab.data.remote.dto.DestinoAporteDto
import com.corpaliv.albalab.data.remote.dto.EstadoPublicoDto
import com.corpaliv.albalab.data.remote.dto.HistoriaDto
import com.corpaliv.albalab.data.remote.dto.HistorialDto
import com.corpaliv.albalab.data.remote.dto.IndicadorDto
import com.corpaliv.albalab.data.remote.dto.LoginRequestDto
import com.corpaliv.albalab.data.remote.dto.LoginResponseDto
import com.corpaliv.albalab.data.remote.dto.MindicadorDto
import com.corpaliv.albalab.data.remote.dto.ProductoDto
import com.corpaliv.albalab.data.remote.dto.ProgramaDto
import com.corpaliv.albalab.data.remote.dto.SolicitudDto
import com.corpaliv.albalab.data.remote.dto.SolicitudRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Cabecera interna que marca las llamadas que deben llevar el JWT (ver [AutenticacionInterceptor]). */
const val CABECERA_REQUIERE_SESION = "X-Requiere-Sesion"

/** ms-admin, a través del api-gateway: inicio de sesión del equipo de vinculación. */
interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body credenciales: LoginRequestDto): LoginResponseDto
}

/** ms-contenidos, a través del api-gateway: contenido público ya filtrado (solo publicado/autorizado). */
interface ContenidosApi {
    @GET("contenidos/programas")
    suspend fun programas(): List<ProgramaDto>

    @GET("contenidos/productos")
    suspend fun productos(@Query("categoria") categoria: String?): List<ProductoDto>

    @GET("contenidos/historias")
    suspend fun historias(): List<HistoriaDto>

    @GET("contenidos/indicadores")
    suspend fun indicadores(): List<IndicadorDto>

    @GET("contenidos/destino-aportes")
    suspend fun destinoAportes(): List<DestinoAporteDto>
}

/** ms-vinculacion, a través del api-gateway. Los dos primeros métodos son públicos; el resto exige JWT. */
interface VinculacionApi {
    @POST("vinculacion/solicitudes")
    suspend fun crear(@Body solicitud: SolicitudRequestDto): EstadoPublicoDto

    @GET("vinculacion/solicitudes/estado/{codigo}")
    suspend fun estado(@Path("codigo") codigo: String): EstadoPublicoDto

    @Headers("$CABECERA_REQUIERE_SESION: true")
    @GET("vinculacion/solicitudes")
    suspend fun listar(@Query("estado") estado: String?): List<SolicitudDto>

    @Headers("$CABECERA_REQUIERE_SESION: true")
    @GET("vinculacion/solicitudes/{id}")
    suspend fun detalle(@Path("id") id: Long): SolicitudDto

    @Headers("$CABECERA_REQUIERE_SESION: true")
    @PATCH("vinculacion/solicitudes/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Body cambio: CambiarEstadoRequestDto): SolicitudDto

    @Headers("$CABECERA_REQUIERE_SESION: true")
    @GET("vinculacion/solicitudes/{id}/historial")
    suspend fun historial(@Path("id") id: Long): List<HistorialDto>

    /** Responde 204 sin cuerpo; un 404 llega como HttpException. */
    @Headers("$CABECERA_REQUIERE_SESION: true")
    @DELETE("vinculacion/solicitudes/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}

/** API pública externa: indicadores económicos de Chile (dólar observado, UF...). */
interface IndicadoresApi {
    @GET("api")
    suspend fun indicadoresDelDia(): MindicadorDto
}
