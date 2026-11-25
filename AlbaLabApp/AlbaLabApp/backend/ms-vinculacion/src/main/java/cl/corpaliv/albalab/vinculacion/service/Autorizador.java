package cl.corpaliv.albalab.vinculacion.service;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;

@Component
public class Autorizador {
    private static final Set<String> ROLES_GESTION = Set.of("GESTOR", "ADMIN");
    private final JwtVerifier verifier;

    public Autorizador(JwtVerifier verifier) {
        this.verifier = verifier;
    }

    /** Devuelve el correo del usuario autenticado si tiene rol GESTOR o ADMIN; si no, lanza 401/403. */
    public String exigirGestion(String cabeceraAuthorization) {
        Claims c = verifier.validar(cabeceraAuthorization);
        if (c == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", "Debes iniciar sesión");
        String rol = c.get("rol", String.class);
        if (rol == null || !ROLES_GESTION.contains(rol)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "SIN_PERMISO", "Tu rol no permite esta acción");
        }
        return c.getSubject();
    }
}
