package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record FacturaItem(
    Long productoId,
    String codigoPrincipal,
    String descripcion,
    BigDecimal cantidad,
    BigDecimal precioUnitario,
    BigDecimal descuento,
    BigDecimal precioTotalSinImpuesto,
    List<FacturaImpuesto> impuestos
) {
  public FacturaItem {
    Objects.requireNonNull(productoId, "productoId");
    Objects.requireNonNull(codigoPrincipal, "codigoPrincipal");
    Objects.requireNonNull(descripcion, "descripcion");
    Objects.requireNonNull(cantidad, "cantidad");
    Objects.requireNonNull(precioUnitario, "precioUnitario");
    Objects.requireNonNull(descuento, "descuento");
    Objects.requireNonNull(precioTotalSinImpuesto, "precioTotalSinImpuesto");
    impuestos = List.copyOf(impuestos);
  }
}
