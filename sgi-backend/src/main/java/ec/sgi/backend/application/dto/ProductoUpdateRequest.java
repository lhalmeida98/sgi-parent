package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductoUpdateRequest(
    @NotBlank String codigo,
    @NotBlank String descripcion,
    @NotNull BigDecimal precioUnitario,
    @NotNull Long categoriaId,
    @NotNull Long impuestoId,
    Boolean vendible,
    String codigoBarras
) {
}
