package cl.corpaliv.albalab.admin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import cl.corpaliv.albalab.admin.service.HashService;

class HashServiceTest {
    private final HashService hash = new HashService();

    @Test
    void hashNoGuardaTextoPlanoYVerifica() {
        String h = hash.hash("Gestor123");
        assertFalse(h.contains("Gestor123"));
        assertTrue(hash.coincide("Gestor123", h));
        assertFalse(hash.coincide("otra", h));
    }

    @Test
    void mismaClaveGeneraHashesDistintosPorSal() {
        assertNotEquals(hash.hash("abc12345"), hash.hash("abc12345"));
    }

    @Test
    void formatoInvalidoNoCoincide() {
        assertFalse(hash.coincide("x", null));
        assertFalse(hash.coincide(null, "pbkdf2$1$a$b"));
        assertFalse(hash.coincide("x", "texto-plano"));
        assertFalse(hash.coincide("x", "pbkdf2$abc$@@$@@"));
    }
}
