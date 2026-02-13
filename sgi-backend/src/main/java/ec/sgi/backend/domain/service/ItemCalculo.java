package ec.sgi.backend.domain.service;

import java.math.BigDecimal;
import java.util.Objects;

public record ItemCalculo(
    Long bodegaId,
    Long productoId,
    String codigoPrincipal,
    String descripcion,
    BigDecimal cantidad,
    BigDecimal precioUnitario,
    BigDecimal descuento,
    String impuestoCodigo,
    String impuestoCodigoPorcentaje,
    BigDecimal impuestoTarifa
) {
  public ItemCalculo {
    Objects.requireNonNull(productoId, "productoId");
    Objects.requireNonNull(codigoPrincipal, "codigoPrincipal");
    Objects.requireNonNull(descripcion, "descripcion");
    Objects.requireNonNull(cantidad, "cantidad");
    Objects.requireNonNull(precioUnitario, "precioUnitario");
    Objects.requireNonNull(descuento, "descuento");
    Objects.requireNonNull(impuestoCodigo, "impuestoCodigo");
    Objects.requireNonNull(impuestoCodigoPorcentaje, "impuestoCodigoPorcentaje");
    Objects.requireNonNull(impuestoTarifa, "impuestoTarifa");
  }
}
