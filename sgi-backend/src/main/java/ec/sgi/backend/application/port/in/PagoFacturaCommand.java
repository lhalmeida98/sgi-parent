package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;
import java.util.Objects;

public record PagoFacturaCommand(
    String formaPago,
    BigDecimal monto
) {
  public PagoFacturaCommand {
    Objects.requireNonNull(formaPago, "formaPago");
    Objects.requireNonNull(monto, "monto");
  }
}
