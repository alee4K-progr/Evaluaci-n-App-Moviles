package cl.corpaliv.albalab.contenidos;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cl.corpaliv.albalab.contenidos.controller.ContenidoController;
import cl.corpaliv.albalab.contenidos.service.ContenidoService;
import cl.corpaliv.albalab.contenidos.service.ContenidoService.*;

class ContenidoControllerTest {
    private ContenidoService servicio;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        servicio = mock(ContenidoService.class);
        mvc = MockMvcBuilders.standaloneSetup(new ContenidoController(servicio)).build();
    }

    @Test
    void listaProgramas() throws Exception {
        when(servicio.programas()).thenReturn(List.of(new ProgramaDto(1L, "Prog", "r", "d", "FORMACION", "COMPRA", "contenidos/imagenes/a.png")));
        mvc.perform(get("/contenidos/programas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Prog"))
                .andExpect(jsonPath("$[0].imagenUrl").value("contenidos/imagenes/a.png"));
    }

    @Test
    void programaInexistenteDevuelve404() throws Exception {
        when(servicio.programa(9L)).thenReturn(Optional.empty());
        mvc.perform(get("/contenidos/programas/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NO_ENCONTRADO"));
    }

    @Test
    void productosPasaLaCategoria() throws Exception {
        when(servicio.productos("SERVICIO")).thenReturn(List.of(new ProductoDto(4L, "Taller", "d", "SERVICIO", 180000, "x", null, true)));
        mvc.perform(get("/contenidos/productos").param("categoria", "SERVICIO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].precioReferencial").value(180000));
    }

    @Test
    void historiasIndicadoresYDestinos() throws Exception {
        when(servicio.historias()).thenReturn(List.of(new HistoriaDto(1L, "t", "p", "h", "r")));
        when(servicio.indicadores()).thenReturn(List.of(new IndicadorDto(1L, "n", 5, "+", "p", "f")));
        when(servicio.destinoAportes()).thenReturn(List.of(new DestinoAporteDto("c", 10)));
        mvc.perform(get("/contenidos/historias")).andExpect(status().isOk()).andExpect(jsonPath("$[0].titulo").value("t"));
        mvc.perform(get("/contenidos/indicadores")).andExpect(status().isOk()).andExpect(jsonPath("$[0].unidad").value("+"));
        mvc.perform(get("/contenidos/destino-aportes")).andExpect(status().isOk()).andExpect(jsonPath("$[0].porcentaje").value(10));
    }
}
