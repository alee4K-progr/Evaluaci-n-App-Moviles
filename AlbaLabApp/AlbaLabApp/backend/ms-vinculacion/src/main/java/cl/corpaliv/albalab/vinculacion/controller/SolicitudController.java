package cl.corpaliv.albalab.vinculacion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.corpaliv.albalab.vinculacion.model.*;
import cl.corpaliv.albalab.vinculacion.service.ApiException;
import cl.corpaliv.albalab.vinculacion.service.Autorizador;
import cl.corpaliv.albalab.vinculacion.service.SolicitudService;
import cl.corpaliv.albalab.vinculacion.service.SolicitudService.NuevaSolicitud;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@RestController
@RequestMapping("/vinculacion/solicitudes")
public class SolicitudController {

    public record CrearSolicitudRequest(
            @NotBlank @Size(max = 120) String nombreContacto,
            @Size(max = 120) String organizacion,
            @Size(max = 160) String email,
            @Size(max = 30) String telefono,
            @NotNull TipoColaboracion tipoColaboracion,
            Long programaId,
            Long productoServicioId,
            @Positive Integer montoClp,
            @Size(max = 1000) String mensaje,
            @AssertTrue(message = "Debes aceptar la política de privacidad") boolean aceptaPrivacidad) {}

    public record CambiarEstadoRequest(@NotNull EstadoSolicitud estado, @Size(max = 500) String observacion) {}

    /** Respuesta publica: no expone datos de contacto. */
    public record EstadoPublicoDto(String codigo, String estado, String fechaCreacion, String fechaActualizacion) {}

    public record SolicitudDto(Long id, String codigo, String nombreContacto, String organizacion, String email,
                               String telefono, String tipoColaboracion, Long programaId, Long productoServicioId,
                               Integer montoClp, String mensaje, String estado, String fechaCreacion,
                               String fechaActualizacion) {}

    public record HistorialDto(Long id, String estadoAnterior, String estadoNuevo, String observacionInterna,
                               String usuarioGestor, String fechaCambio) {}

    private final SolicitudService servicio;
    private final Autorizador autorizador;

    public SolicitudController(SolicitudService servicio, Autorizador autorizador) {
        this.servicio = servicio;
        this.autorizador = autorizador;
    }

    // ---------- Publico (visitante) ----------

    @PostMapping
    public ResponseEntity<EstadoPublicoDto> crear(@Valid @RequestBody CrearSolicitudRequest r) {
        SolicitudColaboracion s = servicio.crear(new NuevaSolicitud(r.nombreContacto(), r.organizacion(), r.email(),
                r.telefono(), r.tipoColaboracion(), r.programaId(), r.productoServicioId(), r.montoClp(),
                r.mensaje(), r.aceptaPrivacidad()));
        return ResponseEntity.status(HttpStatus.CREATED).body(aPublico(s));
    }

    @GetMapping("/estado/{codigo}")
    public EstadoPublicoDto estado(@PathVariable String codigo) {
        return aPublico(servicio.consultarPorCodigo(codigo));
    }

    // ---------- Gestion (GESTOR / ADMIN) ----------

    @GetMapping
    public List<SolicitudDto> listar(@RequestHeader(value = "Authorization", required = false) String auth,
                                     @RequestParam(required = false) EstadoSolicitud estado) {
        autorizador.exigirGestion(auth);
        return servicio.listar(estado).stream().map(this::aDto).toList();
    }

    @GetMapping("/{id}")
    public SolicitudDto detalle(@RequestHeader(value = "Authorization", required = false) String auth,
                                @PathVariable Long id) {
        autorizador.exigirGestion(auth);
        return aDto(servicio.obtener(id));
    }

    @PatchMapping("/{id}/estado")
    public SolicitudDto cambiarEstado(@RequestHeader(value = "Authorization", required = false) String auth,
                                      @PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest r) {
        String usuario = autorizador.exigirGestion(auth);
        return aDto(servicio.cambiarEstado(id, r.estado(), r.observacion(), usuario));
    }

    @GetMapping("/{id}/historial")
    public List<HistorialDto> historial(@RequestHeader(value = "Authorization", required = false) String auth,
                                        @PathVariable Long id) {
        autorizador.exigirGestion(auth);
        return servicio.historial(id).stream().map(h -> new HistorialDto(h.getId(),
                h.getEstadoAnterior() == null ? null : h.getEstadoAnterior().name(), h.getEstadoNuevo().name(),
                h.getObservacionInterna(), h.getUsuarioGestor(), h.getFechaCambio().toString())).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader(value = "Authorization", required = false) String auth,
                                         @PathVariable Long id) {
        autorizador.exigirGestion(auth);
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private EstadoPublicoDto aPublico(SolicitudColaboracion s) {
        return new EstadoPublicoDto(s.getCodigo(), s.getEstado().name(), s.getFechaCreacion().toString(),
                s.getFechaActualizacion().toString());
    }

    private SolicitudDto aDto(SolicitudColaboracion s) {
        return new SolicitudDto(s.getId(), s.getCodigo(), s.getNombreContacto(), s.getOrganizacion(), s.getEmail(),
                s.getTelefono(), s.getTipoColaboracion().name(), s.getProgramaId(), s.getProductoServicioId(),
                s.getMontoClp(), s.getMensaje(), s.getEstado().name(), s.getFechaCreacion().toString(),
                s.getFechaActualizacion().toString());
    }
}
