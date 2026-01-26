package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;
import java.util.Objects;

public record ItemFacturaCommand(
    Long productoId,
    BigDecimal cantidad,
    BigDecimal descuento
) {
  public ItemFacturaCommand {
    Objects.requireNonNull(productoId, "productoId");
    Objects.requireNonNull(cantidad, "cantidad");
    Objects.requireNonNull(descuento, "descuento");
  }
}
