package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FacturaItemRequest(
    Long bodegaId,
    @NotNull Long productoId,
    @NotNull @DecimalMin("0.0001") BigDecimal cantidad,
    @NotNull @DecimalMin("0.00") BigDecimal descuento
) {
}
