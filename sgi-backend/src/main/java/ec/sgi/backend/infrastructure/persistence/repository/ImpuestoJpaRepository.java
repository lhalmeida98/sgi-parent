package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.ImpuestoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImpuestoJpaRepository extends JpaRepository<ImpuestoEntity, Long> {
  List<ImpuestoEntity> findByEmpresaId(Long empresaId);

  Optional<ImpuestoEntity> findByIdAndEmpresaId(Long id, Long empresaId);
}
