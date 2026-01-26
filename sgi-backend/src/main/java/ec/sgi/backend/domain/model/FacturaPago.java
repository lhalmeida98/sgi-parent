package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record FacturaPago(
    String formaPago,
    BigDecimal monto
) {
  public FacturaPago {
    Objects.requireNonNull(formaPago, "formaPago");
    Objects.requireNonNull(monto, "monto");
  }
}
