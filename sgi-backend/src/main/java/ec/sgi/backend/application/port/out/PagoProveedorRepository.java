package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.PagoProveedor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PagoProveedorRepository {
  PagoProveedor save(PagoProveedor pago);

  Optional<PagoProveedor> findById(Long id);

  Optional<PagoProveedor> findByIdAndEmpresaId(Long id, Long empresaId);

  List<PagoProveedor> findByEmpresaId(Long empresaId);

  List<PagoProveedor> findByProveedorId(Long proveedorId);

  List<FechaTotal> sumMontosPorFecha(Long empresaId, LocalDate fechaDesde, LocalDate fechaHasta);

  record FechaTotal(LocalDate fecha, BigDecimal total) {}
}
