package cl.corpaliv.albalab.contenidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.corpaliv.albalab.contenidos.model.ProductoServicio;

public interface ProductoServicioRepository extends JpaRepository<ProductoServicio, Long> {
    List<ProductoServicio> findByPublicadoTrueOrderByIdAsc();
}
