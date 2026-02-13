package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record InventarioBodegaResult(
    Long bodegaId,
    String bodegaNombre,
    BigDecimal stockActual,
    BigDecimal stockReservado,
    BigDecimal stockMinimo,
    BigDecimal stockMaximo,
    String ubicacion,
    BigDecimal costoPromedio,
    BigDecimal margenPorcentaje
) {
}
