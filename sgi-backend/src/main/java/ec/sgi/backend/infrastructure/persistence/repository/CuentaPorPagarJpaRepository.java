package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.CuentaPorPagarEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuentaPorPagarJpaRepository extends JpaRepository<CuentaPorPagarEntity, Long> {
  List<CuentaPorPagarEntity> findByEmpresaId(Long empresaId);

  List<CuentaPorPagarEntity> findByProveedorId(Long proveedorId);

  List<CuentaPorPagarEntity> findByProveedorIdAndEmpresaId(Long proveedorId, Long empresaId);

  Optional<CuentaPorPagarEntity> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<CuentaPorPagarEntity> findByDocumentoProveedorId(Long documentoProveedorId);

  @Query("""
      select coalesce(sum(c.saldo), 0)
      from CuentaPorPagarEntity c
      where c.empresaId = :empresaId
        and c.saldo > 0
        and c.estado not in :estados
      """)
  java.math.BigDecimal sumSaldoPendienteByEmpresaId(
      @Param("empresaId") Long empresaId,
      @Param("estados") List<String> estados
  );

  @Query("""
      select count(c)
      from CuentaPorPagarEntity c
      where c.empresaId = :empresaId
        and c.saldo > 0
        and c.estado not in :estados
        and c.fechaVencimiento is not null
        and c.fechaVencimiento between :desde and :hasta
      """)
  long countVencenEntreFechasByEmpresaId(
      @Param("empresaId") Long empresaId,
      @Param("desde") java.time.LocalDate fechaDesde,
      @Param("hasta") java.time.LocalDate fechaHasta,
      @Param("estados") List<String> estados
  );
}
