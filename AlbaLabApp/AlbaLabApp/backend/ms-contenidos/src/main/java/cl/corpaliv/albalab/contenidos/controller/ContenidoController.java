package cl.corpaliv.albalab.contenidos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.corpaliv.albalab.contenidos.service.ContenidoService;
import cl.corpaliv.albalab.contenidos.service.ContenidoService.*;

@RestController
@RequestMapping("/contenidos")
public class ContenidoController {

    private final ContenidoService servicio;

    public ContenidoController(ContenidoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/programas")
    public List<ProgramaDto> programas() {
        return servicio.programas();
    }

    @GetMapping("/programas/{id}")
    public ResponseEntity<?> programa(@PathVariable Long id) {
        return servicio.programa(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of("error", "NO_ENCONTRADO", "mensaje", "Programa no disponible")));
    }

    @GetMapping("/productos")
    public List<ProductoDto> productos(@RequestParam(required = false) String categoria) {
        return servicio.productos(categoria);
    }

    @GetMapping("/historias")
    public List<HistoriaDto> historias() {
        return servicio.historias();
    }

    @GetMapping("/indicadores")
    public List<IndicadorDto> indicadores() {
        return servicio.indicadores();
    }

    @GetMapping("/destino-aportes")
    public List<DestinoAporteDto> destinoAportes() {
        return servicio.destinoAportes();
    }
}
