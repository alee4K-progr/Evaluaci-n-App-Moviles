package cl.corpaliv.albalab.admin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import cl.corpaliv.albalab.admin.service.JwtService;
import io.jsonwebtoken.Claims;

class JwtServiceTest {
    private static final String SECRETO = "secreto-de-prueba-con-mas-de-32-caracteres";

    @Test
    void secretoCortoSeRechaza() {
        assertThrows(IllegalStateException.class, () -> new JwtService("corto", 10));
        assertThrows(IllegalStateException.class, () -> new JwtService(null, 10));
    }

    @Test
    void tokenGeneradoSeValidaYTraeRol() {
        JwtService jwt = new JwtService(SECRETO, 10);
        String token = jwt.generar("gestor@example.cl", "GESTOR", "Gestora");
        Claims c = jwt.validar(token);
        assertNotNull(c);
        assertEquals("gestor@example.cl", c.getSubject());
        assertEquals("GESTOR", c.get("rol", String.class));
        assertNotNull(jwt.validar("Bearer " + token));
    }

    @Test
    void tokenAlteradoOVencidoOVacioNoValida() {
        JwtService jwt = new JwtService(SECRETO, 10);
        String token = jwt.generar("a@example.cl", "ADMIN", "A");
        assertNull(jwt.validar(token + "x"));
        assertNull(jwt.validar(null));
        assertNull(jwt.validar(" "));
        assertNull(jwt.validar("no.es.token"));
        JwtService otro = new JwtService("otro-secreto-de-prueba-con-mas-de-32-chars", 10);
        assertNull(otro.validar(token));
        JwtService vencido = new JwtService(SECRETO, -1);
        assertNull(vencido.validar(vencido.generar("a@example.cl", "ADMIN", "A")));
    }
}
