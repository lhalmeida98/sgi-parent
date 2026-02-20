package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.RolPermisoEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolPermisoJpaRepository extends JpaRepository<RolPermisoEntity, Long> {
  List<RolPermisoEntity> findByRolIdIn(Collection<Long> rolIds);

  boolean existsByRolIdAndAccionId(Long rolId, Long accionId);

  void deleteByRolId(Long rolId);
}
