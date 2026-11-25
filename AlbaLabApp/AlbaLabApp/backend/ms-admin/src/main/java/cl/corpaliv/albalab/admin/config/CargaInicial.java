package cl.corpaliv.albalab.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.corpaliv.albalab.admin.model.UsuarioGestor;
import cl.corpaliv.albalab.admin.repository.UsuarioRepository;
import cl.corpaliv.albalab.admin.service.HashService;

/** Usuarios FICTICIOS de demostracion (dominio example.cl). Cambiar o desactivar fuera del entorno academico. */
@Configuration
public class CargaInicial {

    @Bean
    CommandLineRunner cargarUsuarios(UsuarioRepository repo, HashService hash,
                                     @Value("${albalab.seed-usuarios:true}") boolean activo) {
        return args -> {
            if (!activo || repo.count() > 0) return;
            repo.save(new UsuarioGestor("Gestora de Vinculación (demo)", "gestor@example.cl", hash.hash("Gestor123"), "GESTOR"));
            repo.save(new UsuarioGestor("Administrador (demo)", "admin@example.cl", hash.hash("Admin123"), "ADMIN"));
        };
    }
}
