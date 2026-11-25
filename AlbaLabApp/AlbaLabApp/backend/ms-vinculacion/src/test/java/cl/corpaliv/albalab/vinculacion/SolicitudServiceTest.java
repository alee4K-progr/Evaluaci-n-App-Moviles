package cl.corpaliv.albalab.vinculacion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import cl.corpaliv.albalab.vinculacion.model.*;
import cl.corpaliv.albalab.vinculacion.repository.HistorialRepository;
import cl.corpaliv.albalab.vinculacion.repository.SolicitudRepository;
import cl.corpaliv.albalab.vinculacion.service.ApiException;
import cl.corpaliv.albalab.vinculacion.service.CodigoGenerator;
import cl.corpaliv.albalab.vinculacion.service.SolicitudService;
import cl.corpaliv.albalab.vinculacion.service.SolicitudService.NuevaSolicitud;

class SolicitudServiceTest {
    private SolicitudRepository repo;
    private HistorialRepository hist;
    private CodigoGenerator codigos;
    private SolicitudService servicio;

    @BeforeEach
    void preparar() {
        repo = mock(SolicitudRepository.class);
        hist = mock(HistorialRepository.class);
        codigos = mock(CodigoGenerator.class);
        Clock reloj = Clock.fixed(Instant.parse("2026-03-10T15:00:00Z"), ZoneId.of("UTC"));
        servicio = new SolicitudService(repo, hist, codigos, reloj);
        when(codigos.nuevo()).thenReturn("AL-ABCD2345");
        when(repo.existsByCodigo(anyString())).thenReturn(false);
        when(repo.save(any(SolicitudColaboracion.class))).thenAnswer(inv -> {
            SolicitudColaboracion s = inv.getArgument(0);
            if (s.getId() == null) s.setId(7L);
            return s;
        });
    }

    private NuevaSolicitud valida() {
        return new NuevaSolicitud("Ana Prueba", " Empresa Demo ", "ana@example.cl", null,
                TipoColaboracion.ALIANZA_RSE, 2L, null, null, "Queremos colaborar", true);
    }

    private NuevaSolicitud con(String nombre, String email, String tel, TipoColaboracion tipo, Integer monto, boolean acepta) {
        return new NuevaSolicitud(nombre, null, email, tel, tipo, null, null, monto, null, acepta);
    }

    private void esperaError(NuevaSolicitud n, String codigo) {
        ApiException e = assertThrows(ApiException.class, () -> servicio.crear(n));
        assertEquals(codigo, e.getCodigo());
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

    private SolicitudColaboracion existente(EstadoSolicitud estado) {
        SolicitudColaboracion s = new SolicitudColaboracion();
        s.setId(7L);
        s.setCodigo("AL-ABCD2345");
        s.setEstado(estado);
        when(repo.findById(7L)).thenReturn(Optional.of(s));
        return s;
    }

    @Test
    void crearGuardaEstadoRecibidaCodigoYHistorialInicial() {
        SolicitudColaboracion s = servicio.crear(valida());
        assertEquals(EstadoSolicitud.RECIBIDA, s.getEstado());
        assertEquals("AL-ABCD2345", s.getCodigo());
        assertEquals("Empresa Demo", s.getOrganizacion());
        assertTrue(s.isAceptaPrivacidad());
        assertEquals(s.getFechaCreacion(), s.getFechaActualizacion());
        ArgumentCaptor<HistorialSolicitud> cap = ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(hist).save(cap.capture());
        assertNull(cap.getValue().getEstadoAnterior());
        assertEquals(EstadoSolicitud.RECIBIDA, cap.getValue().getEstadoNuevo());
    }

    @Test
    void crearAceptaSoloTelefono() {
        assertNotNull(servicio.crear(con("Ana", null, "+56 9 1234 5678", TipoColaboracion.DONACION, 5000, true)));
    }

    @Test
    void crearValidaReglasDeNegocio() {
        esperaError(con(" ", "a@example.cl", null, TipoColaboracion.DONACION, null, true), "NOMBRE_REQUERIDO");
        esperaError(con("Ana", "a@example.cl", null, null, null, true), "TIPO_REQUERIDO");
        esperaError(con("Ana", "a@example.cl", null, TipoColaboracion.COMPRA, null, false), "PRIVACIDAD_REQUERIDA");
        esperaError(con("Ana", " ", " ", TipoColaboracion.COMPRA, null, true), "CONTACTO_REQUERIDO");
        esperaError(con("Ana", "no-es-correo", null, TipoColaboracion.COMPRA, null, true), "EMAIL_INVALIDO");
        esperaError(con("Ana", null, "abc", TipoColaboracion.COMPRA, null, true), "TELEFONO_INVALIDO");
        esperaError(con("Ana", "a@example.cl", null, TipoColaboracion.DONACION, 0, true), "MONTO_INVALIDO");
        esperaError(null, "DATOS_REQUERIDOS");
        verify(repo, never()).save(any());
    }

    @Test
    void crearReintentaSiElCodigoYaExiste() {
        when(codigos.nuevo()).thenReturn("AL-REPETIDO", "AL-NUEVO2345");
        when(repo.existsByCodigo("AL-REPETIDO")).thenReturn(true);
        assertEquals("AL-NUEVO2345", servicio.crear(valida()).getCodigo());
    }

    @Test
    void crearFallaSiNoHayCodigoLibre() {
        when(repo.existsByCodigo(anyString())).thenReturn(true);
        ApiException e = assertThrows(ApiException.class, () -> servicio.crear(valida()));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
    }

    @Test
    void consultarPorCodigoNormalizaYFallaSiNoExiste() {
        when(repo.findByCodigo("AL-ABCD2345")).thenReturn(Optional.of(new SolicitudColaboracion()));
        assertNotNull(servicio.consultarPorCodigo("  al-abcd2345 "));
        when(repo.findByCodigo("AL-NADA")).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ApiException.class, () -> servicio.consultarPorCodigo("AL-NADA")).getStatus());
        assertThrows(ApiException.class, () -> servicio.consultarPorCodigo(null));
    }

    @Test
    void listarConYSinFiltro() {
        when(repo.findAllByOrderByFechaCreacionDesc()).thenReturn(List.of(new SolicitudColaboracion()));
        when(repo.findByEstadoOrderByFechaCreacionDesc(EstadoSolicitud.RECIBIDA)).thenReturn(List.of());
        assertEquals(1, servicio.listar(null).size());
        assertEquals(0, servicio.listar(EstadoSolicitud.RECIBIDA).size());
    }

    @Test
    void cambiarEstadoValidoActualizaYAuditaConUsuario() {
        SolicitudColaboracion s = existente(EstadoSolicitud.RECIBIDA);
        SolicitudColaboracion r = servicio.cambiarEstado(7L, EstadoSolicitud.EN_REVISION, " revisando ", "gestor@example.cl");
        assertEquals(EstadoSolicitud.EN_REVISION, r.getEstado());
        ArgumentCaptor<HistorialSolicitud> cap = ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(hist).save(cap.capture());
        assertEquals(EstadoSolicitud.RECIBIDA, cap.getValue().getEstadoAnterior());
        assertEquals("revisando", cap.getValue().getObservacionInterna());
        assertEquals("gestor@example.cl", cap.getValue().getUsuarioGestor());
        assertSame(s, r);
    }

    @Test
    void cambiarEstadoRechazaTransicionesInvalidas() {
        existente(EstadoSolicitud.RECIBIDA);
        ApiException e = assertThrows(ApiException.class,
                () -> servicio.cambiarEstado(7L, EstadoSolicitud.CONCRETADO, null, "g"));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        existente(EstadoSolicitud.DESCARTADO);
        assertThrows(ApiException.class, () -> servicio.cambiarEstado(7L, EstadoSolicitud.EN_REVISION, null, "g"));
        verify(hist, never()).save(any());
    }

    @Test
    void descartarExigeObservacionYNuloEsInvalido() {
        existente(EstadoSolicitud.EN_REVISION);
        ApiException e = assertThrows(ApiException.class, () -> servicio.cambiarEstado(7L, EstadoSolicitud.DESCARTADO, "  ", "g"));
        assertEquals("OBSERVACION_REQUERIDA", e.getCodigo());
        assertEquals("ESTADO_REQUERIDO", assertThrows(ApiException.class, () -> servicio.cambiarEstado(7L, null, "x", "g")).getCodigo());
        assertEquals(EstadoSolicitud.DESCARTADO,
                servicio.cambiarEstado(7L, EstadoSolicitud.DESCARTADO, "Sin respuesta", "g").getEstado());
    }

    @Test
    void cambiarEstadoDeSolicitudInexistenteDa404() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ApiException.class, () -> servicio.cambiarEstado(99L, EstadoSolicitud.EN_REVISION, null, "g")).getStatus());
    }

    @Test
    void historialYEliminar() {
        existente(EstadoSolicitud.RECIBIDA);
        when(hist.findBySolicitudIdOrderByFechaCambioAsc(7L)).thenReturn(List.of(new HistorialSolicitud()));
        assertEquals(1, servicio.historial(7L).size());
        servicio.eliminar(7L);
        verify(hist).deleteBySolicitudId(7L);
        verify(repo).deleteById(7L);
    }

    @Test
    void eliminarInexistenteNoBorraNada() {
        when(repo.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> servicio.eliminar(5L));
        verify(repo, never()).deleteById(any());
    }
}
