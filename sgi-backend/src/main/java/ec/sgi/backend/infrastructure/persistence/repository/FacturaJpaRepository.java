package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.FacturaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturaJpaRepository extends JpaRepository<FacturaEntity, Long> {
  List<FacturaEntity> findByEstado(String estado);

  List<FacturaEntity> findByEstadoAndEmpresaId(String estado, Long empresaId);
}
