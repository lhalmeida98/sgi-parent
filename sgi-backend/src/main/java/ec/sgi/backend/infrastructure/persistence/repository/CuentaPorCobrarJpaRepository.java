package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.CuentaPorCobrarEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuentaPorCobrarJpaRepository extends JpaRepository<CuentaPorCobrarEntity, Long> {
  List<CuentaPorCobrarEntity> findByEmpresaId(Long empresaId);

  List<CuentaPorCobrarEntity> findByClienteId(Long clienteId);

  List<CuentaPorCobrarEntity> findByClienteIdAndEmpresaId(Long clienteId, Long empresaId);

  Optional<CuentaPorCobrarEntity> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<CuentaPorCobrarEntity> findByDocumentoClienteId(Long documentoClienteId);

  @Query("""
      select coalesce(sum(c.saldo), 0)
      from CuentaPorCobrarEntity c
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
      from CuentaPorCobrarEntity c
      where c.empresaId = :empresaId
        and c.saldo > 0
        and c.estado not in :estados
        and c.fechaVencimiento = :fecha
      """)
  long countPendientesByEmpresaIdAndFechaVencimiento(
      @Param("empresaId") Long empresaId,
      @Param("fecha") java.time.LocalDate fechaVencimiento,
      @Param("estados") List<String> estados
  );
}
