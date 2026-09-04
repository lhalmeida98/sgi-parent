package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.FacturaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

public interface FacturaJpaRepository extends JpaRepository<FacturaEntity, Long> {
  List<FacturaEntity> findByEstado(String estado);

  List<FacturaEntity> findByEstadoAndEmpresaId(String estado, Long empresaId);

  List<FacturaEntity> findByEmpresaId(Long empresaId);

  @Query("""
      select coalesce(sum(f.importeTotal), 0)
      from FacturaEntity f
      where f.empresaId = :empresaId
        and f.fechaEmision between :fechaDesde and :fechaHasta
        and f.estado = :estado
        and upper(f.infoAmbiente) in ('2', 'PRODUCCION')
      """)
  java.math.BigDecimal sumImporteTotalByEmpresaIdAndFechaEmisionBetweenAndEstado(
      @Param("empresaId") Long empresaId,
      @Param("fechaDesde") java.time.LocalDate fechaDesde,
      @Param("fechaHasta") java.time.LocalDate fechaHasta,
      @Param("estado") String estado
  );

  @Query("""
      select f.id as id,
             f.infoEstab as infoEstab,
             f.infoPtoEmi as infoPtoEmi,
             f.infoSecuencial as infoSecuencial,
             f.fechaEmision as fechaEmision,
             f.importeTotal as importeTotal,
             f.estado as estado
      from FacturaEntity f
      where f.empresaId = :empresaId
        and f.fechaEmision between :fechaDesde and :fechaHasta
        and upper(f.infoAmbiente) in ('2', 'PRODUCCION')
      order by f.fechaEmision desc, f.id desc
      """)
  List<FacturaResumenProjection> findUltimasResumen(
      @Param("empresaId") Long empresaId,
      @Param("fechaDesde") java.time.LocalDate fechaDesde,
      @Param("fechaHasta") java.time.LocalDate fechaHasta,
      Pageable pageable
  );

  org.springframework.data.domain.Page<FacturaEntity> findByEmpresaIdAndFechaEmisionBetween(
      Long empresaId,
      java.time.LocalDate fechaDesde,
      java.time.LocalDate fechaHasta,
      org.springframework.data.domain.Pageable pageable);

  @Query("""
      select f
      from FacturaEntity f
      where f.empresaId = :empresaId
        and f.fechaEmision between :fechaDesde and :fechaHasta
        and upper(f.infoAmbiente) in :ambientes
      """)
  org.springframework.data.domain.Page<FacturaEntity> findByEmpresaIdAndFechaEmisionBetweenAndAmbientes(
      @Param("empresaId") Long empresaId,
      @Param("fechaDesde") java.time.LocalDate fechaDesde,
      @Param("fechaHasta") java.time.LocalDate fechaHasta,
      @Param("ambientes") java.util.List<String> ambientes,
      org.springframework.data.domain.Pageable pageable);

  java.util.Optional<FacturaEntity> findByEmpresaIdAndInfoEstabAndInfoPtoEmiAndInfoSecuencial(
      Long empresaId,
      String infoEstab,
      String infoPtoEmi,
      String infoSecuencial);

  java.util.Optional<FacturaEntity> findByEmpresaIdAndInfoSecuencial(Long empresaId, String infoSecuencial);

  interface FacturaResumenProjection {
    Long getId();
    String getInfoEstab();
    String getInfoPtoEmi();
    String getInfoSecuencial();
    java.time.LocalDate getFechaEmision();
    java.math.BigDecimal getImporteTotal();
    String getEstado();
  }
}
