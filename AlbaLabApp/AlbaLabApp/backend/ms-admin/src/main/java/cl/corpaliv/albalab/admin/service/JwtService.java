package cl.corpaliv.albalab.admin.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Emite y valida tokens JWT HS256. El secreto llega por la variable de entorno SECRET. */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiracion-minutos:480}") long expiracionMinutos) {
        byte[] bytes = secreto == null ? new byte[0] : secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("El secreto JWT (SECRET) debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.expiracionMinutos = expiracionMinutos;
    }

    public long getExpiracionMinutos() {
        return expiracionMinutos;
    }

    public String generar(String email, String rol, String nombre) {
        Date ahora = new Date();
        Date vence = new Date(ahora.getTime() + expiracionMinutos * 60_000L);
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .claim("nombre", nombre)
                .issuedAt(ahora)
                .expiration(vence)
                // HS256 explicito: el api-gateway valida con HS256 sin importar el largo del secreto.
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    /** Devuelve los claims si el token es valido; null si esta vencido, alterado o mal formado. */
    public Claims validar(String token) {
        if (token == null || token.isBlank()) return null;
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        try {
            return Jwts.parser().verifyWith(clave).build().parseSignedClaims(jwt).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
