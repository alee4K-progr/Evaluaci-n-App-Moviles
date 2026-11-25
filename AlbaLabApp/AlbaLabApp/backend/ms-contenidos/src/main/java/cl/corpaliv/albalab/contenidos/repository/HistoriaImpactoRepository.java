package cl.corpaliv.albalab.contenidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.corpaliv.albalab.contenidos.model.HistoriaImpacto;

public interface HistoriaImpactoRepository extends JpaRepository<HistoriaImpacto, Long> {
    List<HistoriaImpacto> findByPublicadoTrueOrderByIdAsc();
}
