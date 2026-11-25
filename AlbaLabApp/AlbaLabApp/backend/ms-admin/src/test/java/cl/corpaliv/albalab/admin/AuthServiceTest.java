package cl.corpaliv.albalab.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.corpaliv.albalab.admin.model.UsuarioGestor;
import cl.corpaliv.albalab.admin.repository.UsuarioRepository;
import cl.corpaliv.albalab.admin.service.AuthService;
import cl.corpaliv.albalab.admin.service.HashService;
import cl.corpaliv.albalab.admin.service.JwtService;

class AuthServiceTest {
    private UsuarioRepository repo;
    private HashService hash;
    private AuthService auth;

    @BeforeEach
    void preparar() {
        repo = mock(UsuarioRepository.class);
        hash = new HashService();
        auth = new AuthService(repo, hash, new JwtService("secreto-de-prueba-con-mas-de-32-caracteres", 10));
    }

    private UsuarioGestor usuario(boolean activo) {
        UsuarioGestor u = new UsuarioGestor("Gestora", "gestor@example.cl", hash.hash("Gestor123"), "GESTOR");
        u.setActivo(activo);
        return u;
    }

    @Test
    void loginCorrectoEntregaTokenYRol() {
        when(repo.findByEmailIgnoreCase("gestor@example.cl")).thenReturn(Optional.of(usuario(true)));
        var s = auth.login(" gestor@example.cl ", "Gestor123");
        assertTrue(s.isPresent());
        assertEquals("GESTOR", s.get().rol());
        assertFalse(s.get().token().isBlank());
    }

    @Test
    void claveIncorrectaUsuarioInactivoOInexistenteFallan() {
        when(repo.findByEmailIgnoreCase("gestor@example.cl")).thenReturn(Optional.of(usuario(true)));
        assertTrue(auth.login("gestor@example.cl", "mala").isEmpty());
        when(repo.findByEmailIgnoreCase("inactivo@example.cl")).thenReturn(Optional.of(usuario(false)));
        assertTrue(auth.login("inactivo@example.cl", "Gestor123").isEmpty());
        when(repo.findByEmailIgnoreCase("nadie@example.cl")).thenReturn(Optional.empty());
        assertTrue(auth.login("nadie@example.cl", "x").isEmpty());
    }

    @Test
    void entradasVaciasNoConsultanLaBase() {
        assertTrue(auth.login("", "x").isEmpty());
        assertTrue(auth.login(null, "x").isEmpty());
        assertTrue(auth.login("a@example.cl", "").isEmpty());
        verifyNoInteractions(repo);
    }
}
