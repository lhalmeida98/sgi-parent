package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.InventarioEntity;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventarioJpaRepository extends JpaRepository<InventarioEntity, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<InventarioEntity> findByProductoId(Long productoId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<InventarioEntity> findByProductoIdAndEmpresaId(Long productoId, Long empresaId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      select i from InventarioEntity i
      where i.productoId = :productoId
        and i.empresaId = :empresaId
        and i.bodegaId = :bodegaId
      """)
  Optional<InventarioEntity> lockByProductoIdAndEmpresaIdAndBodegaId(
      @Param("productoId") Long productoId,
      @Param("empresaId") Long empresaId,
      @Param("bodegaId") Long bodegaId
  );

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  java.util.List<InventarioEntity> findAllByProductoIdAndEmpresaId(Long productoId, Long empresaId);

  java.util.List<InventarioEntity> findByEmpresaId(Long empresaId);

  java.util.List<InventarioEntity> findByEmpresaIdAndBodegaId(Long empresaId, Long bodegaId);

  Optional<InventarioEntity> findByProductoIdAndEmpresaIdAndBodegaId(Long productoId, Long empresaId, Long bodegaId);
}
