package com.corpaliv.albalab

import com.corpaliv.albalab.data.catalogo.CatalogoDatos
import com.corpaliv.albalab.data.remote.dto.HistoriaDto
import com.corpaliv.albalab.data.remote.dto.DestinoAporteDto
import com.corpaliv.albalab.data.remote.dto.IndicadorDto
import com.corpaliv.albalab.data.remote.dto.ProgramaDto
import com.corpaliv.albalab.data.repository.CatalogoRepository
import com.corpaliv.albalab.data.repository.Origen
import com.corpaliv.albalab.domain.model.CategoriaPrograma
import com.corpaliv.albalab.viewmodel.ContenidoViewModel
import com.corpaliv.albalab.viewmodel.ProgramasViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExplorarViewModelsTest {

    @Before
    fun prepararMain() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun limpiarMain() = Dispatchers.resetMain()

    private fun repositorio(api: FakeContenidosApi) = CatalogoRepository(api, "http://10.0.2.2:9090/")

    private val programasRemotos = listOf(
        ProgramaDto(1L, "Huerta comunitaria", "Cultivo", "Detalle", "PRODUCCION", "COMPRA", null),
        ProgramaDto(2L, "Empleo remoto", "Trabajo", "Detalle", "EMPLEO", "ALIANZA_RSE", null)
    )

    @Test
    fun programas_muestra_la_lista_del_servidor() {
        val vm = ProgramasViewModel(repositorio(FakeContenidosApi(programas = { programasRemotos })))
        assertEquals(Origen.SERVIDOR, vm.origen.value)
        assertEquals(listOf("1", "2"), vm.programas.value.map { it.id })
    }

    @Test
    fun programas_filtra_por_texto_y_categoria_sobre_la_lista_del_servidor() {
        val vm = ProgramasViewModel(repositorio(FakeContenidosApi(programas = { programasRemotos })))
        vm.cambiarConsulta("huerta")
        assertEquals(listOf("1"), vm.programas.value.map { it.id })
        vm.cambiarConsulta("")
        vm.cambiarCategoria(CategoriaPrograma.EMPLEO)
        assertEquals(listOf("2"), vm.programas.value.map { it.id })
    }

    @Test
    fun programas_sin_conexion_usa_el_contenido_local() {
        val vm = ProgramasViewModel(repositorio(FakeContenidosApi(programas = FakeVinculacionApi.sinRed())))
        assertEquals(Origen.LOCAL, vm.origen.value)
        assertEquals(CatalogoDatos.programas, vm.programas.value)
    }

    @Test
    fun contenido_es_servidor_solo_si_las_tres_consultas_funcionan() {
        val api = FakeContenidosApi(
            indicadores = { listOf(IndicadorDto(1L, "Empresas", 30, "")) },
            historias = { listOf(HistoriaDto(9L, "Caso", "Alguien", "Historia", "Resultado")) },
            destino = { listOf(DestinoAporteDto("Todo", 100)) }
        )
        val vm = ContenidoViewModel(repositorio(api))
        val e = vm.estado.value
        assertEquals(Origen.SERVIDOR, e.origen)
        assertFalse(e.cargando)
        assertEquals(30, e.indicadores.single().valor)
        assertEquals("9", e.casos.single().id)
    }

    @Test
    fun contenido_es_local_si_alguna_consulta_falla() {
        val api = FakeContenidosApi(
            indicadores = { listOf(IndicadorDto(1L, "Empresas", 30, "")) },
            historias = FakeVinculacionApi.sinRed(),
            destino = { listOf(DestinoAporteDto("Todo", 100)) }
        )
        val e = ContenidoViewModel(repositorio(api)).estado.value
        assertEquals(Origen.LOCAL, e.origen)
        assertEquals(CatalogoDatos.casos, e.casos) // lo que falló usa el respaldo
        assertEquals(30, e.indicadores.single().valor) // lo que funcionó se conserva
    }
}
