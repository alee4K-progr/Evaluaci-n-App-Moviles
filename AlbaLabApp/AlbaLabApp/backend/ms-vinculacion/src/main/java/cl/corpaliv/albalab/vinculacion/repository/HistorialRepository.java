package cl.corpaliv.albalab.vinculacion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.corpaliv.albalab.vinculacion.model.HistorialSolicitud;

public interface HistorialRepository extends JpaRepository<HistorialSolicitud, Long> {
    List<HistorialSolicitud> findBySolicitudIdOrderByFechaCambioAsc(Long solicitudId);
    void deleteBySolicitudId(Long solicitudId);
}
