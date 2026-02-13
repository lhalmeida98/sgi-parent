package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.CuentaPorPagarEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaPorPagarJpaRepository extends JpaRepository<CuentaPorPagarEntity, Long> {
  List<CuentaPorPagarEntity> findByEmpresaId(Long empresaId);

  List<CuentaPorPagarEntity> findByProveedorId(Long proveedorId);

  List<CuentaPorPagarEntity> findByProveedorIdAndEmpresaId(Long proveedorId, Long empresaId);

  Optional<CuentaPorPagarEntity> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<CuentaPorPagarEntity> findByDocumentoProveedorId(Long documentoProveedorId);
}
