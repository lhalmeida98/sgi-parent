package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.CobroClienteEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CobroClienteJpaRepository extends JpaRepository<CobroClienteEntity, Long> {
  Optional<CobroClienteEntity> findByIdAndEmpresaId(Long id, Long empresaId);

  List<CobroClienteEntity> findByEmpresaId(Long empresaId);

  List<CobroClienteEntity> findByClienteId(Long clienteId);

  @Query("""
      select c.fecha as fecha,
             coalesce(sum(c.montoTotal), 0) as total
      from CobroClienteEntity c
      where c.empresaId = :empresaId
        and c.fecha between :desde and :hasta
      group by c.fecha
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
