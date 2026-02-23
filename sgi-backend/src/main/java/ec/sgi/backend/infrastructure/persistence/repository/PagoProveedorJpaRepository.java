package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.PagoProveedorEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagoProveedorJpaRepository extends JpaRepository<PagoProveedorEntity, Long> {
  List<PagoProveedorEntity> findByEmpresaId(Long empresaId);

  List<PagoProveedorEntity> findByProveedorId(Long proveedorId);

  Optional<PagoProveedorEntity> findByIdAndEmpresaId(Long id, Long empresaId);

  @Query("""
      select p.fechaPago as fecha,
             coalesce(sum(p.montoTotal), 0) as total
      from PagoProveedorEntity p
      where p.empresaId = :empresaId
        and p.fechaPago between :desde and :hasta
      group by p.fechaPago
      """)
  List<FechaTotalProjection> sumMontosPorFecha(
      @Param("empresaId") Long empresaId,
      @Param("desde") java.time.LocalDate fechaDesde,
      @Param("hasta") java.time.LocalDate fechaHasta
  );

  interface FechaTotalProjection {
    java.time.LocalDate getFecha();
    java.math.BigDecimal getTotal();
  }
}
