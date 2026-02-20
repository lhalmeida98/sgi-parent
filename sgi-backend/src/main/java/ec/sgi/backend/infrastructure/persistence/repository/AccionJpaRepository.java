package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.AccionEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccionJpaRepository extends JpaRepository<AccionEntity, Long> {
  Optional<AccionEntity> findByCodigo(String codigo);

  List<AccionEntity> findByCodigoIn(Collection<String> codigos);

  boolean existsByCodigo(String codigo);

  boolean existsByCodigoAndActivoTrue(String codigo);
}
