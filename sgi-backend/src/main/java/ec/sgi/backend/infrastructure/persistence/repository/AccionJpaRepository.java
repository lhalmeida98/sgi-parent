package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.AccionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccionJpaRepository extends JpaRepository<AccionEntity, Long> {
  List<AccionEntity> findByEmpresaId(Long empresaId);

  Optional<AccionEntity> findByEmpresaIdAndCodigo(Long empresaId, String codigo);

  boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);

  boolean existsByEmpresaIdAndCodigoAndActivoTrue(Long empresaId, String codigo);
}
