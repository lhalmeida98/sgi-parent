package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;
import java.util.Objects;

public record CrearInventarioCommand(
    Long empresaId,
    Long productoId,
    BigDecimal stockActual,
    BigDecimal stockMinimo,
    BigDecimal stockMaximo,
    String ubicacion,
    BigDecimal costoPromedio
) {
  public CrearInventarioCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(productoId, "productoId");
    Objects.requireNonNull(stockActual, "stockActual");
    Objects.requireNonNull(stockMinimo, "stockMinimo");
  }
}
