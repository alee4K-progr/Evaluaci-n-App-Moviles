package cl.corpaliv.albalab.gateway;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import cl.corpaliv.albalab.gateway.config.SecurityConfig;

class SecurityConfigTest {
    private static final String SECRETO = "secreto-de-prueba-con-mas-de-32-caracteres";

    private String token(String secreto, long venceEnMs) throws Exception {
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256),
                new JWTClaimsSet.Builder().subject("gestor@example.cl").claim("rol", "GESTOR")
                        .expirationTime(new Date(System.currentTimeMillis() + venceEnMs)).build());
        jwt.sign(new MACSigner(secreto.getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }

    @Test
    void secretoCortoSeRechaza() {
        assertThrows(IllegalStateException.class, () -> new SecurityConfig("corto"));
        assertThrows(IllegalStateException.class, () -> new SecurityConfig(null));
    }

    @Test
    void decodificaTokenValido() throws Exception {
        ReactiveJwtDecoder d = new SecurityConfig(SECRETO).jwtDecoder();
        Jwt jwt = d.decode(token(SECRETO, 60_000)).block();
        assertNotNull(jwt);
        assertEquals("gestor@example.cl", jwt.getSubject());
        assertEquals("GESTOR", jwt.getClaimAsString("rol"));
    }

    @Test
    void rechazaFirmaDistintaYTokenVencido() throws Exception {
        ReactiveJwtDecoder d = new SecurityConfig(SECRETO).jwtDecoder();
        String otro = token("otro-secreto-de-prueba-con-mas-de-32-chars", 60_000);
        assertThrows(JwtException.class, () -> d.decode(otro).block());
        String vencido = token(SECRETO, -120_000);
        assertThrows(JwtException.class, () -> d.decode(vencido).block());
    }
}
