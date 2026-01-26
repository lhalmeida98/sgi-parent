package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.PreordenEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreordenJpaRepository extends JpaRepository<PreordenEntity, Long> {
  List<PreordenEntity> findByEmpresaId(Long empresaId);

  Optional<PreordenEntity> findByIdAndEmpresaId(Long id, Long empresaId);
}
