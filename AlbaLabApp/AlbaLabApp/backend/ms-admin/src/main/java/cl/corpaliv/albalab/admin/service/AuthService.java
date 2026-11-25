package cl.corpaliv.albalab.admin.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import cl.corpaliv.albalab.admin.model.UsuarioGestor;
import cl.corpaliv.albalab.admin.repository.UsuarioRepository;

@Service
public class AuthService {

    /** Hash valido de una clave inexistente: iguala el tiempo de respuesta cuando el correo no existe. */
    private final String hashFalso;
    private final UsuarioRepository usuarios;
    private final HashService hashService;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarios, HashService hashService, JwtService jwtService) {
        this.usuarios = usuarios;
        this.hashService = hashService;
        this.jwtService = jwtService;
        this.hashFalso = hashService.hash("no-existe");
    }

    public record Sesion(String token, String rol, String nombre, long expiraEnMinutos) {}

    public Optional<Sesion> login(String email, String clave) {
        if (email == null || email.isBlank() || clave == null || clave.isEmpty()) return Optional.empty();
        Optional<UsuarioGestor> u = usuarios.findByEmailIgnoreCase(email.trim());
        if (u.isEmpty()) {
            hashService.coincide(clave, hashFalso);
            return Optional.empty();
        }
        UsuarioGestor usuario = u.get();
        if (!usuario.isActivo() || !hashService.coincide(clave, usuario.getPasswordHash())) {
            return Optional.empty();
        }
        String token = jwtService.generar(usuario.getEmail(), usuario.getRol(), usuario.getNombre());
        return Optional.of(new Sesion(token, usuario.getRol(), usuario.getNombre(), jwtService.getExpiracionMinutos()));
    }
}
