package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record DashboardProductoVendidoResult(
    Long productoId,
    String descripcion,
    BigDecimal cantidadVendida,
    BigDecimal totalVendido
) {
}
