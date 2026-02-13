package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;
import java.util.Objects;

public record ActualizarProductoCommand(
    String codigo,
    String descripcion,
    BigDecimal precioUnitario,
    Long categoriaId,
    Long impuestoId,
    Boolean vendible,
    String codigoBarras
) {
  public ActualizarProductoCommand {
    Objects.requireNonNull(codigo, "codigo");
    Objects.requireNonNull(descripcion, "descripcion");
    Objects.requireNonNull(precioUnitario, "precioUnitario");
    Objects.requireNonNull(categoriaId, "categoriaId");
    Objects.requireNonNull(impuestoId, "impuestoId");
  }
}
