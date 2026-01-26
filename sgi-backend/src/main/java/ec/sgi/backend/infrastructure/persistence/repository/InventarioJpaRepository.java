package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.InventarioEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface InventarioJpaRepository extends JpaRepository<InventarioEntity, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<InventarioEntity> findByProductoId(Long productoId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<InventarioEntity> findByProductoIdAndEmpresaId(Long productoId, Long empresaId);

  java.util.List<InventarioEntity> findByEmpresaId(Long empresaId);
}
