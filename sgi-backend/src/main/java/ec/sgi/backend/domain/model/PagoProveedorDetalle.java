package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record PagoProveedorDetalle(
    Long id,
    Long cuentaPorPagarId,
    BigDecimal montoAplicado
) {
  public PagoProveedorDetalle {
    Objects.requireNonNull(cuentaPorPagarId, "cuentaPorPagarId");
    Objects.requireNonNull(montoAplicado, "montoAplicado");
  }
}
