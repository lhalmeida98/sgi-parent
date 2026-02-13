package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record PreordenItemResult(
    Long bodegaId,
    Long productoId,
    String codigoPrincipal,
    String descripcion,
    BigDecimal cantidad,
    BigDecimal precioUnitario,
    BigDecimal descuento,
    BigDecimal precioTotalSinImpuesto
) {
}
