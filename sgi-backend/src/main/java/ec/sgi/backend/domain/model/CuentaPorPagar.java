package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record CuentaPorPagar(
    Long id,
    Long empresaId,
    Long proveedorId,
    Long documentoProveedorId,
    BigDecimal montoOriginal,
    BigDecimal montoPagado,
    BigDecimal saldo,
    String estado,
    LocalDate fechaVencimiento,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
  public CuentaPorPagar {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(proveedorId, "proveedorId");
    Objects.requireNonNull(documentoProveedorId, "documentoProveedorId");
    Objects.requireNonNull(montoOriginal, "montoOriginal");
    Objects.requireNonNull(montoPagado, "montoPagado");
    Objects.requireNonNull(saldo, "saldo");
    Objects.requireNonNull(estado, "estado");
  }
}
