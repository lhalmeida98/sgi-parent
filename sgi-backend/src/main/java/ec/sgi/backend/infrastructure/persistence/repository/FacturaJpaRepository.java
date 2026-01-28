package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.FacturaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturaJpaRepository extends JpaRepository<FacturaEntity, Long> {
  List<FacturaEntity> findByEstado(String estado);

  List<FacturaEntity> findByEstadoAndEmpresaId(String estado, Long empresaId);

  List<FacturaEntity> findByEmpresaId(Long empresaId);

  org.springframework.data.domain.Page<FacturaEntity> findByEmpresaIdAndFechaEmisionBetween(
      Long empresaId,
      java.time.LocalDate fechaDesde,
      java.time.LocalDate fechaHasta,
      org.springframework.data.domain.Pageable pageable);

  java.util.Optional<FacturaEntity> findByEmpresaIdAndInfoEstabAndInfoPtoEmiAndInfoSecuencial(
      Long empresaId,
      String infoEstab,
      String infoPtoEmi,
      String infoSecuencial);

  java.util.Optional<FacturaEntity> findByEmpresaIdAndInfoSecuencial(Long empresaId, String infoSecuencial);
}
