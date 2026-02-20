package ec.sgi.backend.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CobroClienteCreateRequest(
    @NotNull Long clienteId,
    @NotNull LocalDate fecha,
    @NotBlank String formaPago,
    String referencia,
    @NotNull BigDecimal montoTotal,
    String observacion,
    @Valid List<CobroClienteDetalleRequest> detalles
) {
}
