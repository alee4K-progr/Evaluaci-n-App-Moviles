package com.corpaliv.albalab

import com.corpaliv.albalab.data.catalogo.CatalogoDatos
import com.corpaliv.albalab.data.remote.ContenidosApi
import com.corpaliv.albalab.data.remote.RedFactory
import com.corpaliv.albalab.data.repository.CatalogoRepository
import com.corpaliv.albalab.data.repository.Origen
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Prueba el contrato JSON real (Retrofit + Gson) de ms-contenidos contra un servidor simulado. */
class CatalogoRepositoryTest {

    private lateinit var servidor: MockWebServer
    private lateinit var repo: CatalogoRepository
    private lateinit var base: String

    @Before
    fun preparar() {
        servidor = MockWebServer().also { it.start() }
        base = servidor.url("/").toString()
        repo = CatalogoRepository(RedFactory.crear(base, ContenidosApi::class.java), base)
    }

    @After
    fun cerrar() {
        runCatching { servidor.shutdown() } // un test puede haberlo cerrado antes
    }

    private fun json(cuerpo: String, codigo: Int = 200) {
        servidor.enqueue(
            MockResponse().setResponseCode(codigo)
                .setHeader("Content-Type", "application/json").setBody(cuerpo)
        )
    }

    @Test
    fun programas_del_servidor_se_mapean_con_imagen_absoluta() = runBlocking {
        json(
            """[{"id":1,"nombre":"Desde el servidor","resumen":"r","descripcion":"d",
                 "categoria":"FORMACION","tipoSugerido":"VOLUNTARIADO","imagenUrl":"contenidos/imagenes/programa-1.png"}]"""
        )
        val r = repo.programas()
        assertEquals(Origen.SERVIDOR, r.origen)
        assertEquals(1, r.datos.size)
        assertEquals("Desde el servidor", r.datos[0].titulo)
        assertEquals("1", r.datos[0].id)
        assertEquals(base + "contenidos/imagenes/programa-1.png", r.datos[0].imagenUrl)
        assertEquals("/contenidos/programas", servidor.takeRequest().path)
    }

    @Test
    fun elementos_invalidos_se_descartan_pero_los_validos_se_conservan() = runBlocking {
        json(
            """[{"id":1,"nombre":"Bueno","categoria":"EMPLEO","tipoSugerido":"ALIANZA_RSE"},
                {"id":2,"nombre":"Malo","categoria":"INVENTADA","tipoSugerido":"ALIANZA_RSE"}]"""
        )
        val r = repo.programas()
        assertEquals(Origen.SERVIDOR, r.origen)
        assertEquals(listOf("1"), r.datos.map { it.id })
    }

    @Test
    fun error_http_500_usa_el_contenido_local() = runBlocking {
        json("{}", codigo = 500)
        val r = repo.programas()
        assertEquals(Origen.LOCAL, r.origen)
        assertEquals(CatalogoDatos.programas, r.datos)
    }

    @Test
    fun json_invalido_usa_el_contenido_local() = runBlocking {
        json("esto no es json")
        val r = repo.productos()
        assertEquals(Origen.LOCAL, r.origen)
        assertEquals(CatalogoDatos.productos, r.datos)
    }

    @Test
    fun lista_vacia_del_servidor_usa_el_contenido_local() = runBlocking {
        json("[]")
        val r = repo.indicadores()
        assertEquals(Origen.LOCAL, r.origen)
        assertEquals(CatalogoDatos.indicadores, r.datos)
    }

    @Test
    fun servidor_caido_usa_el_contenido_local() = runBlocking {
        servidor.shutdown()
        val r = repo.casosImpacto()
        assertEquals(Origen.LOCAL, r.origen)
        assertEquals(CatalogoDatos.casos, r.datos)
    }

    @Test
    fun historias_e_indicadores_usan_las_rutas_del_gateway() = runBlocking {
        json("""[{"id":1,"titulo":"Caso","protagonista":"X (ficticio)","historia":"h","resultado":"r"}]""")
        json("""[{"id":1,"nombre":"Empresas aliadas","valor":25,"unidad":"+"}]""")
        val casos = repo.casosImpacto()
        val indicadores = repo.indicadores()
        assertEquals("Caso", casos.datos.single().titulo)
        assertEquals("Empresas aliadas", indicadores.datos.single().etiqueta)
        assertEquals("+", indicadores.datos.single().sufijo)
        assertEquals("/contenidos/historias", servidor.takeRequest().path)
        assertEquals("/contenidos/indicadores", servidor.takeRequest().path)
    }

    @Test
    fun destino_de_aportes_se_lee_del_servidor() = runBlocking {
        json("""[{"concepto":"Formación","porcentaje":60},{"concepto":"Gestión","porcentaje":40}]""")
        val r = repo.destinoAportes()
        assertEquals(Origen.SERVIDOR, r.origen)
        assertEquals(listOf(60, 40), r.datos.map { it.porcentaje })
    }
}
