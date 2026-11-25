package cl.corpaliv.albalab.vinculacion;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import cl.corpaliv.albalab.vinculacion.service.ApiException;
import cl.corpaliv.albalab.vinculacion.service.Autorizador;
import cl.corpaliv.albalab.vinculacion.service.CodigoGenerator;
import cl.corpaliv.albalab.vinculacion.service.JwtVerifier;

class AutorizadorTest {
    @Test
    void secretoCortoSeRechaza() {
        assertThrows(IllegalStateException.class, () -> new JwtVerifier("corto"));
    }

    @Test
    void sinCabeceraEs401() {
        Autorizador a = new Autorizador(new JwtVerifier("secreto-de-prueba-con-mas-de-32-caracteres"));
        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(ApiException.class, () -> a.exigirGestion(null)).getStatus());
        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(ApiException.class, () -> a.exigirGestion("Token abc")).getStatus());
    }

    @Test
    void codigosTienenFormatoEsperado() {
        CodigoGenerator g = new CodigoGenerator();
        String c = g.nuevo();
        assertTrue(c.matches("AL-[A-HJ-NP-Z2-9]{8}"), c);
        assertNotEquals(c, g.nuevo());
    }
}
