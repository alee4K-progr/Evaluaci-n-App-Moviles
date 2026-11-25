package cl.corpaliv.albalab.contenidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.corpaliv.albalab.contenidos.model.DestinoAporte;

public interface DestinoAporteRepository extends JpaRepository<DestinoAporte, Long> {
    List<DestinoAporte> findAllByOrderByIdAsc();
}
