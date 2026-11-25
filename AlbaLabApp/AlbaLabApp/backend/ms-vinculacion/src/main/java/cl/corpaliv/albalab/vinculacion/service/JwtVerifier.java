package cl.corpaliv.albalab.vinculacion.service;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Valida los JWT emitidos por ms-admin (defensa en profundidad: el api-gateway ya los valida). */
@Component
public class JwtVerifier {
    private final SecretKey clave;

    public JwtVerifier(@Value("${jwt.secret}") String secreto) {
        byte[] bytes = secreto == null ? new byte[0] : secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalStateException("El secreto JWT (SECRET) debe tener al menos 32 caracteres");
        this.clave = Keys.hmacShaKeyFor(bytes);
    }

    public Claims validar(String cabecera) {
        if (cabecera == null || !cabecera.startsWith("Bearer ")) return null;
        try {
            return Jwts.parser().verifyWith(clave).build().parseSignedClaims(cabecera.substring(7)).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
