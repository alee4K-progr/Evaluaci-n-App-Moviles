package cl.corpaliv.albalab.contenidos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cl.corpaliv.albalab.contenidos.model.*;
import cl.corpaliv.albalab.contenidos.repository.*;

/** Reglas de visibilidad: solo se entrega lo publicado; las historias ademas deben estar autorizadas y anonimizadas. */
@Service
public class ContenidoService {

    public record ProgramaDto(Long id, String nombre, String resumen, String descripcion, String categoria,
                              String tipoSugerido, String imagenUrl) {}

    public record ProductoDto(Long id, String nombre, String descripcion, String categoria, Integer precioReferencial,
                              String destinoAporte, String imagenUrl, boolean disponible) {}

    public record HistoriaDto(Long id, String titulo, String protagonista, String historia, String resultado) {}

    public record IndicadorDto(Long id, String nombre, int valor, String unidad, String periodo, String fuente) {}

    public record DestinoAporteDto(String concepto, int porcentaje) {}

    private final ProgramaRepository programas;
    private final ProductoServicioRepository productos;
    private final HistoriaImpactoRepository historias;
    private final IndicadorImpactoRepository indicadores;
    private final DestinoAporteRepository destinos;

    public ContenidoService(ProgramaRepository programas, ProductoServicioRepository productos,
                            HistoriaImpactoRepository historias, IndicadorImpactoRepository indicadores,
                            DestinoAporteRepository destinos) {
        this.programas = programas;
        this.productos = productos;
        this.historias = historias;
        this.indicadores = indicadores;
        this.destinos = destinos;
    }

    public List<ProgramaDto> programas() {
        return programas.findByPublicadoTrueOrderByIdAsc().stream().map(this::aDto).toList();
    }

    public Optional<ProgramaDto> programa(Long id) {
        return programas.findById(id).filter(Programa::isPublicado).map(this::aDto);
    }

    /** Filtra por categoria (sin distinguir mayusculas); categoria nula o vacia devuelve todo lo publicado. */
    public List<ProductoDto> productos(String categoria) {
        boolean filtrar = categoria != null && !categoria.isBlank();
        return productos.findByPublicadoTrueOrderByIdAsc().stream()
                .filter(p -> !filtrar || categoria.trim().equalsIgnoreCase(p.getCategoria()))
                .map(p -> new ProductoDto(p.getId(), p.getNombre(), p.getDescripcion(), p.getCategoria(),
                        p.getPrecioReferencial(), p.getDestinoAporte(), p.getImagenUrl(), p.isDisponible()))
                .toList();
    }

    public List<HistoriaDto> historias() {
        return historias.findByPublicadoTrueOrderByIdAsc().stream()
                .filter(h -> h.isAutorizada() && h.isAnonimizada())
                .map(h -> new HistoriaDto(h.getId(), h.getTitulo(), h.getProtagonista(), h.getHistoria(), h.getResultado()))
                .toList();
    }

    public List<IndicadorDto> indicadores() {
        return indicadores.findByPublicadoTrueOrderByIdAsc().stream()
                .map(i -> new IndicadorDto(i.getId(), i.getNombre(), i.getValor(), i.getUnidad(), i.getPeriodo(), i.getFuente()))
                .toList();
    }

    public List<DestinoAporteDto> destinoAportes() {
        return destinos.findAllByOrderByIdAsc().stream()
                .map(d -> new DestinoAporteDto(d.getConcepto(), d.getPorcentaje())).toList();
    }

    private ProgramaDto aDto(Programa p) {
        return new ProgramaDto(p.getId(), p.getNombre(), p.getResumen(), p.getDescripcion(), p.getCategoria(),
                p.getTipoSugerido(), p.getImagenUrl());
    }
}
