package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ImpuestoCreateRequest(
    @NotBlank String codigo,
    @NotBlank String codigoPorcentaje,
    @NotNull BigDecimal tarifa,
    String descripcion,
    Boolean activo
) {
}
