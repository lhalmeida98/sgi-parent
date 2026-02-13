package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.BodegaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodegaJpaRepository extends JpaRepository<BodegaEntity, Long> {
  List<BodegaEntity> findByEmpresaId(Long empresaId);

  Optional<BodegaEntity> findByIdAndEmpresaId(Long id, Long empresaId);
}
