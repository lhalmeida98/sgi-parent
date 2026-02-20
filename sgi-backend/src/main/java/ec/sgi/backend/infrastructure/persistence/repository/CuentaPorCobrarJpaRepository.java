package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.CuentaPorCobrarEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaPorCobrarJpaRepository extends JpaRepository<CuentaPorCobrarEntity, Long> {
  List<CuentaPorCobrarEntity> findByEmpresaId(Long empresaId);

  List<CuentaPorCobrarEntity> findByClienteId(Long clienteId);

  List<CuentaPorCobrarEntity> findByClienteIdAndEmpresaId(Long clienteId, Long empresaId);

  Optional<CuentaPorCobrarEntity> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<CuentaPorCobrarEntity> findByDocumentoClienteId(Long documentoClienteId);
}
