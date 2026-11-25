package com.corpaliv.albalab

import com.corpaliv.albalab.data.remote.aDominio
import com.corpaliv.albalab.data.remote.aSolicitudDto
import com.corpaliv.albalab.data.remote.dto.EstadoPublicoDto
import com.corpaliv.albalab.data.remote.dto.HistorialDto
import com.corpaliv.albalab.data.remote.dto.IndicadorDto
import com.corpaliv.albalab.data.remote.dto.ProductoDto
import com.corpaliv.albalab.data.remote.dto.ProgramaDto
import com.corpaliv.albalab.data.remote.resolverUrl
import com.corpaliv.albalab.domain.model.CategoriaProducto
import com.corpaliv.albalab.domain.model.CategoriaPrograma
import com.corpaliv.albalab.domain.model.EstadoSolicitud
import com.corpaliv.albalab.domain.model.TipoColaboracion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class MapeadoresTest {

    private val base = "http://10.0.2.2:9090/"
    private val programaOk = ProgramaDto(1L, "Título", "Resumen", "Detalle", "EMPLEO", "ALIANZA_RSE", null)

    @Test
    fun programa_valido_se_convierte_a_dominio() {
        val p = programaOk.aDominio(base)
        assertNotNull(p)
        assertEquals("1", p!!.id)
        assertEquals(CategoriaPrograma.EMPLEO, p.categoria)
        assertEquals(TipoColaboracion.ALIANZA_RSE, p.tipoSugerido)
        assertNull(p.imagenUrl)
    }

    @Test
    fun programa_con_categoria_o_tipo_desconocido_o_sin_id_se_descarta() {
        assertNull(programaOk.copy(categoria = "NO_EXISTE").aDominio(base))
        assertNull(programaOk.copy(tipoSugerido = "NADA").aDominio(base))
        assertNull(programaOk.copy(id = null).aDominio(base))
        assertNull(programaOk.copy(nombre = null).aDominio(base))
    }

    @Test
    fun producto_acepta_precio_nulo_y_rechaza_categoria_invalida() {
        val ok = ProductoDto(5L, "Servicio", null, "SERVICIO", null, null, "contenidos/imagenes/x.png", true).aDominio(base)
        assertNotNull(ok)
        assertNull(ok!!.precioReferencial)
        assertEquals(CategoriaProducto.SERVICIO, ok.categoria)
        assertEquals("http://10.0.2.2:9090/contenidos/imagenes/x.png", ok.imagenUrl)
        assertNull(ProductoDto(1L, "n", null, "NADA", 1, null, null, true).aDominio(base))
    }

    @Test
    fun indicador_sin_valor_se_descarta() {
        assertNull(IndicadorDto(1L, "Empresas", null, "").aDominio())
        assertEquals(25, IndicadorDto(1L, "Empresas", 25, null).aDominio()!!.valor)
    }

    @Test
    fun resolverUrl_maneja_relativas_absolutas_y_vacias() {
        assertNull(resolverUrl(base, null))
        assertNull(resolverUrl(base, "  "))
        assertEquals("https://cdn.example.cl/a.png", resolverUrl(base, "https://cdn.example.cl/a.png"))
        assertEquals("http://10.0.2.2:9090/a/b.png", resolverUrl("http://10.0.2.2:9090", "/a/b.png"))
    }

    @Test
    fun registro_local_se_convierte_a_solicitud_con_contacto_y_referencias() {
        val epoch = LocalDate.of(2026, 12, 5).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val entidad = registroDePrueba(fechaContacto = epoch).copy(programaId = "2", productoId = "x", telefono = "")
        val dto = entidad.aSolicitudDto()
        assertEquals("VOLUNTARIADO", dto.tipoColaboracion)
        assertEquals("persona.prueba@example.cl", dto.email)
        assertNull(dto.telefono)          // vacío -> null (el servidor exige al menos un medio)
        assertNull(dto.organizacion)
        assertEquals(2L, dto.programaId)
        assertNull(dto.productoServicioId) // id no numérico se ignora en vez de fallar
        assertTrue(dto.aceptaPrivacidad)
        assertTrue(dto.mensaje!!.contains("Comuna: Providencia"))
        assertTrue(dto.mensaje!!.contains("2026-12-05"))
    }

    @Test
    fun mensaje_para_el_servidor_no_supera_el_maximo() {
        val largo = registroDePrueba().copy(mensaje = "a".repeat(2000)).aSolicitudDto()
        assertEquals(1000, largo.mensaje!!.length)
        val vacio = registroDePrueba().copy(mensaje = "").aSolicitudDto()
        assertTrue(vacio.mensaje!!.startsWith("[Comuna:"))
    }

    @Test
    fun estado_publico_e_historial_validan_el_estado() {
        assertEquals(EstadoSolicitud.CONTACTADO, EstadoPublicoDto("AL-1", "CONTACTADO", "a", "b").aDominio()!!.estado)
        assertNull(EstadoPublicoDto("AL-1", "INVENTADO", "a", "b").aDominio())
        assertNull(EstadoPublicoDto(null, "RECIBIDA", "a", "b").aDominio())
        val h = HistorialDto(1L, null, "RECIBIDA", "ingreso", "gestor@example.cl", "2026-03-10T12:00:00").aDominio()
        assertNotNull(h)
        assertNull(h!!.estadoAnterior)
        assertNull(HistorialDto(1L, "RECIBIDA", "NADA", null, null, null).aDominio())
    }

    @Test
    fun solicitud_de_gestion_requiere_id_codigo_nombre_y_estado_valido() {
        val ok = solicitudDtoDePrueba().aDominio()
        assertNotNull(ok)
        assertEquals(TipoColaboracion.DONACION, ok!!.tipo)
        assertNull(solicitudDtoDePrueba().copy(id = null).aDominio())
        assertNull(solicitudDtoDePrueba().copy(codigo = null).aDominio())
        assertNull(solicitudDtoDePrueba().copy(estado = "X").aDominio())
        assertNull(solicitudDtoDePrueba().copy(nombreContacto = null).aDominio())
    }
}
