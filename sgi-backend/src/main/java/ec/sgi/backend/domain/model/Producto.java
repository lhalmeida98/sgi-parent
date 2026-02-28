package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Producto(
    Long id,
    Long empresaId,
    String codigo,
    String descripcion,
    BigDecimal precioUnitario,
    Long categoriaId,
    Long impuestoId,
    Long proveedorId,
    boolean vendible,
    String codigoBarras
) {
  public Producto {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(codigo, "codigo");
    Objects.requireNonNull(descripcion, "descripcion");
    Objects.requireNonNull(precioUnitario, "precioUnitario");
    Objects.requireNonNull(categoriaId, "categoriaId");
    Objects.requireNonNull(impuestoId, "impuestoId");
  }
}
