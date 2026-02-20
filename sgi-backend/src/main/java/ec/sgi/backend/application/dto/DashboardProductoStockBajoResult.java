package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record DashboardProductoStockBajoResult(
    Long productoId,
    String descripcion,
    BigDecimal stockActual,
    BigDecimal stockMinimo
) {
}
