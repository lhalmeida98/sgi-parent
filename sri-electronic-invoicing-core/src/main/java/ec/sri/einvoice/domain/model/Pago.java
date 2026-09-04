package ec.sri.einvoice.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Pago(
    String formaPago,
    BigDecimal total,
    Integer plazo,
    String unidadTiempo
) {
  public Pago {
    Objects.requireNonNull(formaPago, "formaPago");
    Objects.requireNonNull(total, "total");
  }
}
