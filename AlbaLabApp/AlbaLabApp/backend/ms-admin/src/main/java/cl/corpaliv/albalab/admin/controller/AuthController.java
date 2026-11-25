package cl.corpaliv.albalab.admin.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.corpaliv.albalab.admin.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/auth")
public class AuthController {

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record LoginResponse(String token, String rol, String nombre, long expiraEnMinutos) {}

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req.email(), req.password())
                .<ResponseEntity<?>>map(s -> ResponseEntity.ok(
                        new LoginResponse(s.token(), s.rol(), s.nombre(), s.expiraEnMinutos())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "CREDENCIALES_INVALIDAS", "mensaje", "Correo o contraseña incorrectos")));
    }
}
