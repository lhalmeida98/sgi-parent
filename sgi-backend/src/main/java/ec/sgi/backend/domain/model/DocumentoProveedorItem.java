package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record DocumentoProveedorItem(
    Long id,
    Long bodegaId,
    Long productoId,
    String codigoPrincipal,
    String descripcion,
    BigDecimal cantidad,
    BigDecimal costoUnitario,
    BigDecimal subtotal
) {
  public DocumentoProveedorItem {
    Objects.requireNonNull(bodegaId, "bodegaId");
    Objects.requireNonNull(cantidad, "cantidad");
    Objects.requireNonNull(costoUnitario, "costoUnitario");
    Objects.requireNonNull(subtotal, "subtotal");
  }
}
