package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaEstado;
import java.util.List;
import java.util.Optional;

public interface FacturaRepository {
  Factura save(Factura factura);

  Optional<Factura> findById(Long id);

  List<Factura> findByEstado(FacturaEstado estado);

  List<Factura> findByEstadoAndEmpresaId(FacturaEstado estado, Long empresaId);

  List<Factura> findByEmpresaId(Long empresaId);

  org.springframework.data.domain.Page<Factura> findByEmpresaIdAndFechaEmisionBetween(
      Long empresaId,
      java.time.LocalDate fechaDesde,
      java.time.LocalDate fechaHasta,
      org.springframework.data.domain.Pageable pageable
  );

  java.util.Optional<Factura> findByEmpresaIdAndInfoEstabAndInfoPtoEmiAndInfoSecuencial(
      Long empresaId,
      String infoEstab,
      String infoPtoEmi,
      String infoSecuencial);

  java.util.Optional<Factura> findByEmpresaIdAndInfoSecuencial(Long empresaId, String infoSecuencial);
}
