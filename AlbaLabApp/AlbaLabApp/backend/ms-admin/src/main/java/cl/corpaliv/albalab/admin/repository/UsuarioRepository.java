package cl.corpaliv.albalab.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.corpaliv.albalab.admin.model.UsuarioGestor;

public interface UsuarioRepository extends JpaRepository<UsuarioGestor, Long> {
    Optional<UsuarioGestor> findByEmailIgnoreCase(String email);
}
