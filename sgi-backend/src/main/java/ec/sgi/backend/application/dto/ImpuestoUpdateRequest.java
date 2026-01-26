package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ImpuestoUpdateRequest(
    @NotBlank String codigo,
    @NotBlank String codigoPorcentaje,
    @NotNull BigDecimal tarifa,
    String descripcion,
    Boolean activo
) {
}
