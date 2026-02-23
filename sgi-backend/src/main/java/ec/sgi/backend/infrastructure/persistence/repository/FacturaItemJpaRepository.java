package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.FacturaItemEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FacturaItemJpaRepository extends JpaRepository<FacturaItemEntity, Long> {
  @Query("""
      select i.productoId as productoId,
             i.descripcion as descripcion,
             coalesce(sum(i.cantidad), 0) as cantidad,
             coalesce(sum(i.precioTotalSinImpuesto), 0) as total
      from FacturaItemEntity i
      join i.factura f
      where f.empresaId = :empresaId
        and f.fechaEmision between :fechaDesde and :fechaHasta
        and f.estado = :estado
      group by i.productoId, i.descripcion
      order by sum(i.precioTotalSinImpuesto) desc
      """)
  List<ProductoVendidoProjection> findProductosMasVendidos(
      @Param("empresaId") Long empresaId,
      @Param("fechaDesde") java.time.LocalDate fechaDesde,
      @Param("fechaHasta") java.time.LocalDate fechaHasta,
      @Param("estado") String estado,
      Pageable pageable
  );

  interface ProductoVendidoProjection {
    Long getProductoId();
    String getDescripcion();
    java.math.BigDecimal getCantidad();
    java.math.BigDecimal getTotal();
  }
}
