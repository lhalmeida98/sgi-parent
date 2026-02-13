package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PagoProveedorDetalleRequest(
    @NotNull Long cuentaPorPagarId,
    @NotNull BigDecimal montoAplicado
) {
}
