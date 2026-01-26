package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FacturaPagoRequest(
    @NotBlank String formaPago,
    @NotNull @DecimalMin("0.01") BigDecimal monto
) {
}
