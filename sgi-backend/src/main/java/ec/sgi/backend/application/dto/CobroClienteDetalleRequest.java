package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CobroClienteDetalleRequest(
    @NotNull Long cuentaPorCobrarId,
    @NotNull BigDecimal montoAplicado
) {
}
