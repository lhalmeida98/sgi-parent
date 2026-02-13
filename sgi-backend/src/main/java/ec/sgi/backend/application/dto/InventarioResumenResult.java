package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record InventarioResumenResult(
    Long productoId,
    String productoNombre,
    BigDecimal stockGlobal,
    BigDecimal stockReservadoGlobal,
    BigDecimal costoPromedioGlobal,
    BigDecimal precioVenta,
    BigDecimal margenPorcentaje,
    List<InventarioBodegaResult> bodegas
) {
}
