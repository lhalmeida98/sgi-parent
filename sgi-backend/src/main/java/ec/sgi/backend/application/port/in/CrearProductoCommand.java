package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;

public record CrearProductoCommand(
    Long empresaId,
    String codigo,
    String descripcion,
    BigDecimal precioUnitario,
    Long categoriaId,
    Long impuestoId,
    Boolean vendible,
    String codigoBarras
) {
  public CrearProductoCommand {
    java.util.Objects.requireNonNull(empresaId, "empresaId");
    java.util.Objects.requireNonNull(codigo, "codigo");
    java.util.Objects.requireNonNull(descripcion, "descripcion");
    java.util.Objects.requireNonNull(precioUnitario, "precioUnitario");
    java.util.Objects.requireNonNull(categoriaId, "categoriaId");
    java.util.Objects.requireNonNull(impuestoId, "impuestoId");
  }
}
