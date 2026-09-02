package ec.sgi.backend.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record FacturaCreateRequest(
    @NotNull Long empresaId,
    @NotNull Long clienteId,
    Long preordenId,
    String dirEstablecimiento,
    @NotNull LocalDate fechaEmision,
    @NotBlank String moneda,
    @NotBlank String codigoNumerico,
    String observaciones,
    @Valid @NotEmpty List<FacturaItemRequest> items,
    @Valid @NotEmpty @Size(max = 2) List<FacturaPagoRequest> pagos
) {
}
