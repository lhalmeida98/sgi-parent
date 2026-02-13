package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record PagoProveedor(
    Long id,
    Long empresaId,
    Long proveedorId,
    LocalDate fechaPago,
    String formaPago,
    String referencia,
    BigDecimal montoTotal,
    String observacion,
    List<PagoProveedorDetalle> detalles,
    LocalDateTime creadoEn
) {
  public PagoProveedor {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(proveedorId, "proveedorId");
    Objects.requireNonNull(fechaPago, "fechaPago");
    Objects.requireNonNull(formaPago, "formaPago");
    Objects.requireNonNull(montoTotal, "montoTotal");
    detalles = detalles == null ? List.of() : List.copyOf(detalles);
  }
}
