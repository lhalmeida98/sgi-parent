package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.BodegaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodegaJpaRepository extends JpaRepository<BodegaEntity, Long> {
  List<BodegaEntity> findByEmpresaId(Long empresaId);
}
