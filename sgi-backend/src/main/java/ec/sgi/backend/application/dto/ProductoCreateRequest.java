package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductoCreateRequest(
    @NotBlank String codigo,
    @NotBlank String descripcion,
    @NotNull BigDecimal precioUnitario,
    @NotNull Long categoriaId,
    @NotNull Long impuestoId,
    Long proveedorId,
    @NotNull Long bodegaId,
    @NotNull @DecimalMin("0.00") BigDecimal costo,
    Boolean vendible,
    String codigoBarras
) {
}
