package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record DocumentoProveedorPreviewItemResult(
    Long bodegaId,
    String codigoPrincipal,
    String descripcion,
    BigDecimal cantidad,
    BigDecimal costoUnitario,
    BigDecimal subtotal
) {
}
