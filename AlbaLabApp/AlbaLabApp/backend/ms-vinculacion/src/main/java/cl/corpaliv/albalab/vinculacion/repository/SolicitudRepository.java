package cl.corpaliv.albalab.vinculacion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.corpaliv.albalab.vinculacion.model.EstadoSolicitud;
import cl.corpaliv.albalab.vinculacion.model.SolicitudColaboracion;

public interface SolicitudRepository extends JpaRepository<SolicitudColaboracion, Long> {
    Optional<SolicitudColaboracion> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<SolicitudColaboracion> findAllByOrderByFechaCreacionDesc();
    List<SolicitudColaboracion> findByEstadoOrderByFechaCreacionDesc(EstadoSolicitud estado);
}
