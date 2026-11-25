package com.corpaliv.albalab.data.remote

import com.corpaliv.albalab.data.local.RegistroInteresEntity
import com.corpaliv.albalab.data.remote.dto.DestinoAporteDto
import com.corpaliv.albalab.data.remote.dto.EstadoPublicoDto
import com.corpaliv.albalab.data.remote.dto.HistoriaDto
import com.corpaliv.albalab.data.remote.dto.HistorialDto
import com.corpaliv.albalab.data.remote.dto.IndicadorDto
import com.corpaliv.albalab.data.remote.dto.ProductoDto
import com.corpaliv.albalab.data.remote.dto.ProgramaDto
import com.corpaliv.albalab.data.remote.dto.SolicitudDto
import com.corpaliv.albalab.data.remote.dto.SolicitudRequestDto
import com.corpaliv.albalab.domain.model.CambioEstado
import com.corpaliv.albalab.domain.model.CasoImpacto
import com.corpaliv.albalab.domain.model.CategoriaProducto
import com.corpaliv.albalab.domain.model.CategoriaPrograma
import com.corpaliv.albalab.domain.model.DestinoAporte
import com.corpaliv.albalab.domain.model.EstadoPublico
import com.corpaliv.albalab.domain.model.EstadoSolicitud
import com.corpaliv.albalab.domain.model.Indicador
import com.corpaliv.albalab.domain.model.Producto
import com.corpaliv.albalab.domain.model.Programa
import com.corpaliv.albalab.domain.model.SolicitudGestion
import com.corpaliv.albalab.domain.model.TipoColaboracion
import java.time.Instant
import java.time.ZoneOffset

/*
 * DTO <-> dominio. Cada función devuelve null si falta un dato obligatorio o trae un valor
 * desconocido; el repositorio descarta esos elementos (mapNotNull) para no romper la interfaz.
 */

/** El backend entrega rutas relativas ("contenidos/imagenes/x.png"); se resuelven contra la URL del gateway. */
fun resolverUrl(base: String, relativa: String?): String? {
    val ruta = relativa?.trim().orEmpty()
    if (ruta.isEmpty()) return null
    if (ruta.startsWith("http://") || ruta.startsWith("https://")) return ruta
    return base.trimEnd('/') + "/" + ruta.trimStart('/')
}

fun ProgramaDto.aDominio(baseUrl: String): Programa? {
    val cat = CategoriaPrograma.entries.firstOrNull { it.name == categoria } ?: return null
    val tipo = TipoColaboracion.desdeNombre(tipoSugerido) ?: return null
    return Programa(
        id = id?.toString() ?: return null,
        titulo = nombre ?: return null,
        resumen = resumen.orEmpty(),
        detalle = descripcion.orEmpty(),
        categoria = cat,
        tipoSugerido = tipo,
        imagenUrl = resolverUrl(baseUrl, imagenUrl)
    )
}

fun ProductoDto.aDominio(baseUrl: String): Producto? {
    val cat = CategoriaProducto.entries.firstOrNull { it.name == categoria } ?: return null
    return Producto(
        id = id?.toString() ?: return null,
        nombre = nombre ?: return null,
        descripcion = descripcion.orEmpty(),
        precioReferencial = precioReferencial,
        destinoAporte = destinoAporte.orEmpty(),
        categoria = cat,
        imagenUrl = resolverUrl(baseUrl, imagenUrl)
    )
}

fun HistoriaDto.aDominio(): CasoImpacto? {
    return CasoImpacto(
        id = id?.toString() ?: return null,
        titulo = titulo ?: return null,
        protagonista = protagonista.orEmpty(),
        historia = historia.orEmpty(),
        resultado = resultado.orEmpty()
    )
}

fun IndicadorDto.aDominio(): Indicador? {
    return Indicador(
        etiqueta = nombre ?: return null,
        valor = valor ?: return null,
        sufijo = unidad.orEmpty()
    )
}

fun DestinoAporteDto.aDominio(): DestinoAporte? {
    return DestinoAporte(
        concepto = concepto ?: return null,
        porcentaje = porcentaje ?: return null
    )
}

private const val LARGO_MAXIMO_MENSAJE_SERVIDOR = 1000

/**
 * La solicitud del servidor no tiene campos para comuna ni fecha preferida de contacto, así que se
 * agregan al final del mensaje para que el equipo de vinculación los vea.
 */
internal fun RegistroInteresEntity.mensajeParaServidor(): String {
    val fecha = Instant.ofEpochMilli(fechaContacto).atZone(ZoneOffset.UTC).toLocalDate()
    val nota = "[Comuna: $comuna · Contacto preferido: $fecha]"
    val base = mensaje.trim()
    return (if (base.isEmpty()) nota else "$base\n$nota").take(LARGO_MAXIMO_MENSAJE_SERVIDOR)
}

fun RegistroInteresEntity.aSolicitudDto(): SolicitudRequestDto = SolicitudRequestDto(
    nombreContacto = nombre,
    organizacion = organizacion.ifBlank { null },
    email = correo.ifBlank { null },
    telefono = telefono.ifBlank { null },
    tipoColaboracion = tipo,
    programaId = programaId?.toLongOrNull(),
    productoServicioId = productoId?.toLongOrNull(),
    montoClp = monto,
    mensaje = mensajeParaServidor(),
    aceptaPrivacidad = aceptaTerminos,
    claveIdempotencia = claveIdempotencia
)

fun EstadoPublicoDto.aDominio(): EstadoPublico? {
    return EstadoPublico(
        codigo = codigo ?: return null,
        estado = EstadoSolicitud.desdeNombre(estado) ?: return null,
        fechaCreacion = fechaCreacion.orEmpty(),
        fechaActualizacion = fechaActualizacion.orEmpty()
    )
}

fun SolicitudDto.aDominio(): SolicitudGestion? {
    return SolicitudGestion(
        id = id ?: return null,
        codigo = codigo ?: return null,
        nombreContacto = nombreContacto ?: return null,
        organizacion = organizacion,
        email = email,
        telefono = telefono,
        tipo = TipoColaboracion.desdeNombre(tipoColaboracion),
        programaId = programaId,
        productoServicioId = productoServicioId,
        montoClp = montoClp,
        mensaje = mensaje,
        estado = EstadoSolicitud.desdeNombre(estado) ?: return null,
        fechaCreacion = fechaCreacion.orEmpty(),
        fechaActualizacion = fechaActualizacion.orEmpty()
    )
}

fun HistorialDto.aDominio(): CambioEstado? {
    return CambioEstado(
        estadoAnterior = EstadoSolicitud.desdeNombre(estadoAnterior),
        estadoNuevo = EstadoSolicitud.desdeNombre(estadoNuevo) ?: return null,
        observacion = observacionInterna,
        usuario = usuarioGestor,
        fecha = fechaCambio.orEmpty()
    )
}
