package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.RolEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolJpaRepository extends JpaRepository<RolEntity, Long> {
  List<RolEntity> findByEmpresaId(Long empresaId);

  Optional<RolEntity> findByEmpresaIdAndNombre(Long empresaId, String nombre);

  boolean existsByEmpresaIdAndNombre(Long empresaId, String nombre);
}
