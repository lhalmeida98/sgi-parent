package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.ProveedorEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorJpaRepository extends JpaRepository<ProveedorEntity, Long> {
  List<ProveedorEntity> findByEmpresaId(Long empresaId);

  Optional<ProveedorEntity> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<ProveedorEntity> findByEmpresaIdAndIdentificacion(Long empresaId, String identificacion);

  boolean existsByEmpresaIdAndIdentificacion(Long empresaId, String identificacion);
}
