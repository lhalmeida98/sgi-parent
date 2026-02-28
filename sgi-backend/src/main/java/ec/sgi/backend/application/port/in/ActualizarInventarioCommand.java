package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;
import java.util.Objects;

public record ActualizarInventarioCommand(
    BigDecimal stockActual,
    BigDecimal stockMinimo,
    BigDecimal stockMaximo,
    String ubicacion,
    BigDecimal costoPromedio
) {
  public ActualizarInventarioCommand {
    Objects.requireNonNull(stockActual, "stockActual");
    Objects.requireNonNull(stockMinimo, "stockMinimo");
  }
}
