package cl.corpaliv.albalab.contenidos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.corpaliv.albalab.contenidos.model.*;
import cl.corpaliv.albalab.contenidos.repository.*;
import cl.corpaliv.albalab.contenidos.service.ContenidoService;

class ContenidoServiceTest {
    private ProgramaRepository programas;
    private ProductoServicioRepository productos;
    private HistoriaImpactoRepository historias;
    private IndicadorImpactoRepository indicadores;
    private DestinoAporteRepository destinos;
    private ContenidoService servicio;

    @BeforeEach
    void preparar() {
        programas = mock(ProgramaRepository.class);
        productos = mock(ProductoServicioRepository.class);
        historias = mock(HistoriaImpactoRepository.class);
        indicadores = mock(IndicadorImpactoRepository.class);
        destinos = mock(DestinoAporteRepository.class);
        servicio = new ContenidoService(programas, productos, historias, indicadores, destinos);
    }

    private Programa programa(boolean publicado) {
        return new Programa("Prog", "res", "desc", "FORMACION", "VOLUNTARIADO", "contenidos/imagenes/x.png", publicado);
    }

    @Test
    void programasEntregaSoloLoQueDevuelveElRepositorioPublicado() {
        when(programas.findByPublicadoTrueOrderByIdAsc()).thenReturn(List.of(programa(true)));
        var lista = servicio.programas();
        assertEquals(1, lista.size());
        assertEquals("Prog", lista.get(0).nombre());
        assertEquals("VOLUNTARIADO", lista.get(0).tipoSugerido());
    }

    @Test
    void programaNoPublicadoOInexistenteNoSeEntrega() {
        when(programas.findById(1L)).thenReturn(Optional.of(programa(false)));
        when(programas.findById(2L)).thenReturn(Optional.of(programa(true)));
        when(programas.findById(3L)).thenReturn(Optional.empty());
        assertTrue(servicio.programa(1L).isEmpty());
        assertTrue(servicio.programa(2L).isPresent());
        assertTrue(servicio.programa(3L).isEmpty());
    }

    @Test
    void productosFiltraPorCategoriaSinDistinguirMayusculas() {
        when(productos.findByPublicadoTrueOrderByIdAsc()).thenReturn(List.of(
                new ProductoServicio("A", "d", "PRODUCTO", 100, "x", null, true),
                new ProductoServicio("B", "d", "SERVICIO", null, "y", null, true)));
        assertEquals(2, servicio.productos(null).size());
        assertEquals(2, servicio.productos("  ").size());
        var servicios = servicio.productos("servicio");
        assertEquals(1, servicios.size());
        assertEquals("B", servicios.get(0).nombre());
        assertTrue(servicio.productos("RSE").isEmpty());
    }

    @Test
    void historiasExigenAutorizacionYAnonimizacion() {
        when(historias.findByPublicadoTrueOrderByIdAsc()).thenReturn(List.of(
                new HistoriaImpacto("ok", "P", "h", "r", true, true, true),
                new HistoriaImpacto("sinAutorizar", "P", "h", "r", false, true, true),
                new HistoriaImpacto("sinAnonimizar", "P", "h", "r", true, false, true)));
        var lista = servicio.historias();
        assertEquals(1, lista.size());
        assertEquals("ok", lista.get(0).titulo());
    }

    @Test
    void indicadoresYDestinoAportesSeMapean() {
        when(indicadores.findByPublicadoTrueOrderByIdAsc()).thenReturn(List.of(new IndicadorImpacto("Empresas", 25, "", "2025", "fuente", true)));
        when(destinos.findAllByOrderByIdAsc()).thenReturn(List.of(new DestinoAporte("Formación", 45)));
        assertEquals(25, servicio.indicadores().get(0).valor());
        assertEquals(45, servicio.destinoAportes().get(0).porcentaje());
    }
}
