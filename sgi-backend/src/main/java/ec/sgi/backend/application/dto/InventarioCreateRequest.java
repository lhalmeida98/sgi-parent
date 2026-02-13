package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InventarioCreateRequest(
    @NotNull Long bodegaId,
    @NotNull Long productoId,
    @NotNull @DecimalMin("0.00") BigDecimal stockActual,
    @NotNull @DecimalMin("0.00") BigDecimal stockMinimo,
    @DecimalMin("0.00") BigDecimal stockMaximo,
    String ubicacion,
    @DecimalMin("0.00") BigDecimal costoPromedio
) {
}
