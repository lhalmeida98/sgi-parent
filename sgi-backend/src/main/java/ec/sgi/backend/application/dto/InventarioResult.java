package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventarioResult(
    Long id,
    Long productoId,
    BigDecimal stockActual,
    BigDecimal stockReservado,
    BigDecimal stockMinimo,
    BigDecimal stockMaximo,
    String ubicacion,
    BigDecimal costoPromedio,
    LocalDateTime actualizadoEn
) {
}
