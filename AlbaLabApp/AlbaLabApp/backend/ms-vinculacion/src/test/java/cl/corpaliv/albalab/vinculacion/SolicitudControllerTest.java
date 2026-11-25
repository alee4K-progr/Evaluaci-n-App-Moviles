package cl.corpaliv.albalab.vinculacion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cl.corpaliv.albalab.vinculacion.controller.ManejadorErrores;
import cl.corpaliv.albalab.vinculacion.controller.SolicitudController;
import cl.corpaliv.albalab.vinculacion.model.*;
import cl.corpaliv.albalab.vinculacion.service.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class SolicitudControllerTest {
    private static final String SECRETO = "secreto-de-prueba-con-mas-de-32-caracteres";
    private SolicitudService servicio;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        servicio = mock(SolicitudService.class);
        Autorizador autorizador = new Autorizador(new JwtVerifier(SECRETO));
        mvc = MockMvcBuilders.standaloneSetup(new SolicitudController(servicio, autorizador))
                .setControllerAdvice(new ManejadorErrores()).build();
    }

    private String token(String rol) {
        SecretKey k = Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8));
        return "Bearer " + Jwts.builder().subject("gestor@example.cl").claim("rol", rol)
                .expiration(new Date(System.currentTimeMillis() + 60_000)).signWith(k, Jwts.SIG.HS256).compact();
    }

    private SolicitudColaboracion solicitud(EstadoSolicitud estado) {
        SolicitudColaboracion s = new SolicitudColaboracion();
        s.setId(1L);
        s.setCodigo("AL-ABCD2345");
        s.setNombreContacto("Ana");
        s.setEmail("ana@example.cl");
        s.setTipoColaboracion(TipoColaboracion.DONACION);
        s.setEstado(estado);
        s.setFechaCreacion(LocalDateTime.of(2026, 3, 10, 12, 0));
        s.setFechaActualizacion(LocalDateTime.of(2026, 3, 10, 12, 0));
        return s;
    }

    private static final String CUERPO_VALIDO =
            "{\"nombreContacto\":\"Ana\",\"email\":\"ana@example.cl\",\"tipoColaboracion\":\"DONACION\",\"montoClp\":5000,\"aceptaPrivacidad\":true}";

    @Test
    void crearDevuelve201SinExponerContacto() throws Exception {
        when(servicio.crear(any())).thenReturn(solicitud(EstadoSolicitud.RECIBIDA));
        mvc.perform(post("/vinculacion/solicitudes").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("AL-ABCD2345"))
                .andExpect(jsonPath("$.estado").value("RECIBIDA"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void crearConDatosInvalidosDevuelve400() throws Exception {
        mvc.perform(post("/vinculacion/solicitudes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreContacto\":\"\",\"tipoColaboracion\":\"DONACION\",\"aceptaPrivacidad\":false}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("DATOS_INVALIDOS"));
        mvc.perform(post("/vinculacion/solicitudes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreContacto\":\"Ana\",\"tipoColaboracion\":\"INVENTADO\",\"aceptaPrivacidad\":true}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("FORMATO_INVALIDO"));
        verify(servicio, never()).crear(any());
    }

    @Test
    void consultaPublicaPorCodigo() throws Exception {
        when(servicio.consultarPorCodigo("AL-ABCD2345")).thenReturn(solicitud(EstadoSolicitud.EN_REVISION));
        mvc.perform(get("/vinculacion/solicitudes/estado/AL-ABCD2345"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("EN_REVISION"));
    }

    @Test
    void codigoInexistenteDevuelve404() throws Exception {
        when(servicio.consultarPorCodigo("AL-X")).thenThrow(new ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "NO_ENCONTRADA", "no"));
        mvc.perform(get("/vinculacion/solicitudes/estado/AL-X")).andExpect(status().isNotFound());
    }

    @Test
    void gestionExigeTokenValido() throws Exception {
        mvc.perform(get("/vinculacion/solicitudes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/vinculacion/solicitudes").header("Authorization", "Bearer basura")).andExpect(status().isUnauthorized());
        mvc.perform(get("/vinculacion/solicitudes").header("Authorization", token("VISITANTE"))).andExpect(status().isForbidden());
        verifyNoInteractions(servicio);
    }

    @Test
    void gestorListaConFiltroDeEstado() throws Exception {
        when(servicio.listar(EstadoSolicitud.RECIBIDA)).thenReturn(List.of(solicitud(EstadoSolicitud.RECIBIDA)));
        mvc.perform(get("/vinculacion/solicitudes").param("estado", "RECIBIDA").header("Authorization", token("GESTOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].email").value("ana@example.cl"));
        mvc.perform(get("/vinculacion/solicitudes").param("estado", "NOEXISTE").header("Authorization", token("GESTOR")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void detalleCambioDeEstadoHistorialYEliminar() throws Exception {
        when(servicio.obtener(1L)).thenReturn(solicitud(EstadoSolicitud.RECIBIDA));
        when(servicio.cambiarEstado(1L, EstadoSolicitud.EN_REVISION, "ok", "gestor@example.cl"))
                .thenReturn(solicitud(EstadoSolicitud.EN_REVISION));
        HistorialSolicitud h = new HistorialSolicitud();
        h.setId(3L);
        h.setEstadoNuevo(EstadoSolicitud.RECIBIDA);
        h.setFechaCambio(LocalDateTime.of(2026, 3, 10, 12, 0));
        when(servicio.historial(1L)).thenReturn(List.of(h));

        mvc.perform(get("/vinculacion/solicitudes/1").header("Authorization", token("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.codigo").value("AL-ABCD2345"));
        mvc.perform(patch("/vinculacion/solicitudes/1/estado").header("Authorization", token("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"EN_REVISION\",\"observacion\":\"ok\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("EN_REVISION"));
        mvc.perform(get("/vinculacion/solicitudes/1/historial").header("Authorization", token("GESTOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].estadoNuevo").value("RECIBIDA"))
                .andExpect(jsonPath("$[0].estadoAnterior").doesNotExist());
        mvc.perform(delete("/vinculacion/solicitudes/1").header("Authorization", token("GESTOR")))
                .andExpect(status().isNoContent());
        verify(servicio).eliminar(1L);
    }

    @Test
    void cambioDeEstadoSinEstadoDevuelve400() throws Exception {
        mvc.perform(patch("/vinculacion/solicitudes/1/estado").header("Authorization", token("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"observacion\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }
}
