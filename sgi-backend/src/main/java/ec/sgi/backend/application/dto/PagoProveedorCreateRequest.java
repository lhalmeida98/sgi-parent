package ec.sgi.backend.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PagoProveedorCreateRequest(
    @NotNull Long proveedorId,
    @NotNull LocalDate fechaPago,
    @NotBlank String formaPago,
    String referencia,
    @NotNull BigDecimal montoTotal,
    String observacion,
    @Valid List<PagoProveedorDetalleRequest> detalles
) {
}
