package cl.corpaliv.albalab.vinculacion.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.corpaliv.albalab.vinculacion.model.*;
import cl.corpaliv.albalab.vinculacion.repository.HistorialRepository;
import cl.corpaliv.albalab.vinculacion.repository.SolicitudRepository;

@Service
public class SolicitudService {

    /** Datos de entrada ya validados por formato; las reglas de negocio se verifican aqui. */
    public record NuevaSolicitud(String nombreContacto, String organizacion, String email, String telefono,
                                 TipoColaboracion tipoColaboracion, Long programaId, Long productoServicioId,
                                 Integer montoClp, String mensaje, boolean aceptaPrivacidad) {}

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern TELEFONO = Pattern.compile("^[+0-9 ()-]{7,20}$");

    private final SolicitudRepository solicitudes;
    private final HistorialRepository historial;
    private final CodigoGenerator codigos;
    private final Clock reloj;

    public SolicitudService(SolicitudRepository solicitudes, HistorialRepository historial,
                            CodigoGenerator codigos, Clock reloj) {
        this.solicitudes = solicitudes;
        this.historial = historial;
        this.codigos = codigos;
        this.reloj = reloj;
    }

    @Transactional
    public SolicitudColaboracion crear(NuevaSolicitud n) {
        validar(n);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        SolicitudColaboracion s = new SolicitudColaboracion();
        s.setCodigo(codigoUnico());
        s.setNombreContacto(n.nombreContacto().trim());
        s.setOrganizacion(vacioANulo(n.organizacion()));
        s.setEmail(vacioANulo(n.email()));
        s.setTelefono(vacioANulo(n.telefono()));
        s.setTipoColaboracion(n.tipoColaboracion());
        s.setProgramaId(n.programaId());
        s.setProductoServicioId(n.productoServicioId());
        s.setMontoClp(n.montoClp());
        s.setMensaje(vacioANulo(n.mensaje()));
        s.setAceptaPrivacidad(true);
        s.setEstado(EstadoSolicitud.RECIBIDA);
        s.setFechaCreacion(ahora);
        s.setFechaActualizacion(ahora);
        SolicitudColaboracion guardada = solicitudes.save(s);
        registrar(guardada.getId(), null, EstadoSolicitud.RECIBIDA, "Solicitud ingresada desde la app", "app-visitante", ahora);
        return guardada;
    }

    public SolicitudColaboracion consultarPorCodigo(String codigo) {
        String c = codigo == null ? "" : codigo.trim().toUpperCase();
        return solicitudes.findByCodigo(c).orElseThrow(this::noEncontrada);
    }

    public List<SolicitudColaboracion> listar(EstadoSolicitud estado) {
        return estado == null ? solicitudes.findAllByOrderByFechaCreacionDesc()
                : solicitudes.findByEstadoOrderByFechaCreacionDesc(estado);
    }

    public SolicitudColaboracion obtener(Long id) {
        return solicitudes.findById(id).orElseThrow(this::noEncontrada);
    }

    public List<HistorialSolicitud> historial(Long id) {
        obtener(id);
        return historial.findBySolicitudIdOrderByFechaCambioAsc(id);
    }

    @Transactional
    public SolicitudColaboracion cambiarEstado(Long id, EstadoSolicitud nuevo, String observacion, String usuario) {
        if (nuevo == null) throw new ApiException(HttpStatus.BAD_REQUEST, "ESTADO_REQUERIDO", "Indica el nuevo estado");
        SolicitudColaboracion s = obtener(id);
        EstadoSolicitud actual = s.getEstado();
        if (!actual.puedePasarA(nuevo)) {
            throw new ApiException(HttpStatus.CONFLICT, "TRANSICION_INVALIDA",
                    "No se puede pasar de " + actual + " a " + nuevo);
        }
        String obs = vacioANulo(observacion);
        if (nuevo == EstadoSolicitud.DESCARTADO && obs == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OBSERVACION_REQUERIDA", "Descartar exige una observación");
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        s.setEstado(nuevo);
        s.setFechaActualizacion(ahora);
        SolicitudColaboracion guardada = solicitudes.save(s);
        registrar(id, actual, nuevo, obs, usuario, ahora);
        return guardada;
    }

    @Transactional
    public void eliminar(Long id) {
        obtener(id);
        historial.deleteBySolicitudId(id);
        solicitudes.deleteById(id);
    }

    // ---- reglas de negocio ----

    void validar(NuevaSolicitud n) {
        if (n == null) throw invalida("DATOS_REQUERIDOS", "Faltan datos de la solicitud");
        if (n.nombreContacto() == null || n.nombreContacto().isBlank()) throw invalida("NOMBRE_REQUERIDO", "El nombre es obligatorio");
        if (n.tipoColaboracion() == null) throw invalida("TIPO_REQUERIDO", "Selecciona el tipo de interés");
        if (!n.aceptaPrivacidad()) throw invalida("PRIVACIDAD_REQUERIDA", "Debes aceptar la política de privacidad");
        String email = vacioANulo(n.email());
        String tel = vacioANulo(n.telefono());
        if (email == null && tel == null) throw invalida("CONTACTO_REQUERIDO", "Indica al menos un correo o un teléfono");
        if (email != null && !EMAIL.matcher(email).matches()) throw invalida("EMAIL_INVALIDO", "El correo no tiene un formato válido");
        if (tel != null && !TELEFONO.matcher(tel).matches()) throw invalida("TELEFONO_INVALIDO", "El teléfono no tiene un formato válido");
        if (n.montoClp() != null && n.montoClp() <= 0) throw invalida("MONTO_INVALIDO", "El monto debe ser mayor que cero");
    }

    private String codigoUnico() {
        for (int i = 0; i < 10; i++) {
            String c = codigos.nuevo();
            if (!solicitudes.existsByCodigo(c)) return c;
        }
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "CODIGO_NO_DISPONIBLE", "No se pudo generar un código, intenta nuevamente");
    }

    private void registrar(Long id, EstadoSolicitud anterior, EstadoSolicitud nuevo, String obs, String usuario, LocalDateTime fecha) {
        HistorialSolicitud h = new HistorialSolicitud();
        h.setSolicitudId(id);
        h.setEstadoAnterior(anterior);
        h.setEstadoNuevo(nuevo);
        h.setObservacionInterna(obs);
        h.setUsuarioGestor(usuario);
        h.setFechaCambio(fecha);
        historial.save(h);
    }

    private ApiException invalida(String codigo, String mensaje) {
        return new ApiException(HttpStatus.BAD_REQUEST, codigo, mensaje);
    }

    private ApiException noEncontrada() {
        return new ApiException(HttpStatus.NOT_FOUND, "NO_ENCONTRADA", "Solicitud no encontrada");
    }

    private static String vacioANulo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
