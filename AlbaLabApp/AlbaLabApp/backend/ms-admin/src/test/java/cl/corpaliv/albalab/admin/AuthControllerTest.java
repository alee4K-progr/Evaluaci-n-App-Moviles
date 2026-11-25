package cl.corpaliv.albalab.admin;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cl.corpaliv.albalab.admin.controller.AuthController;
import cl.corpaliv.albalab.admin.controller.ManejadorErrores;
import cl.corpaliv.albalab.admin.service.AuthService;

class AuthControllerTest {
    private AuthService servicio;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        servicio = mock(AuthService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(servicio))
                .setControllerAdvice(new ManejadorErrores()).build();
    }

    @Test
    void loginOkDevuelve200ConToken() throws Exception {
        when(servicio.login("gestor@example.cl", "Gestor123"))
                .thenReturn(Optional.of(new AuthService.Sesion("tok", "GESTOR", "Gestora", 480)));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"gestor@example.cl\",\"password\":\"Gestor123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("tok"))
                .andExpect(jsonPath("$.rol").value("GESTOR"));
    }

    @Test
    void credencialesMalasDevuelven401() throws Exception {
        when(servicio.login(anyString(), anyString())).thenReturn(Optional.empty());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@example.cl\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void cuerpoIncompletoDevuelve400() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("no-json"))
                .andExpect(status().isBadRequest());
    }
}
