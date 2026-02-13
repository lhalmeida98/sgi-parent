package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.PagoProveedorEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoProveedorJpaRepository extends JpaRepository<PagoProveedorEntity, Long> {
  List<PagoProveedorEntity> findByEmpresaId(Long empresaId);

  List<PagoProveedorEntity> findByProveedorId(Long proveedorId);

  Optional<PagoProveedorEntity> findByIdAndEmpresaId(Long id, Long empresaId);
}
