package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.RolEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolJpaRepository extends JpaRepository<RolEntity, Long> {
  Optional<RolEntity> findByNombre(String nombre);

  List<RolEntity> findByNombreIn(Collection<String> nombres);

  boolean existsByNombre(String nombre);
}
