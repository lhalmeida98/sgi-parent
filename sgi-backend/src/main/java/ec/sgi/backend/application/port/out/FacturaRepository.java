package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaEstado;
import ec.sgi.backend.application.dto.DashboardFacturaItemResult;
import ec.sgi.backend.application.dto.DashboardProductoVendidoResult;
import java.util.List;
import java.util.Optional;

public interface FacturaRepository {
  Factura save(Factura factura);

  Optional<Factura> findById(Long id);

  List<Factura> findByEstado(FacturaEstado estado);

  List<Factura> findByEstadoAndEmpresaId(FacturaEstado estado, Long empresaId);

  List<Factura> findByEmpresaId(Long empresaId);

  java.math.BigDecimal sumImporteTotalByEmpresaIdAndFechaEmisionBetweenAndEstado(
      Long empresaId,
      java.time.LocalDate fechaDesde,
      java.time.LocalDate fechaHasta,
      FacturaEstado estado
  );

  List<DashboardFacturaItemResult> findUltimasFacturasResumen(
      Long empresaId,
      java.time.LocalDate fechaDesde,
      java.time.LocalDate fechaHasta,
      int limit
  );

  List<DashboardProductoVendidoResult> findProductosMasVendidos(
      Long empresaId,
      java.time.LocalDate fechaDesde,
      java.time.LocalDate fechaHasta,
      FacturaEstado estado,
      int limit
  );

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
