package ec.sgi.backend.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PreordenCreateRequest(
    Long empresaId,
    @NotNull Long clienteId,
    @NotBlank String dirEstablecimiento,
    @NotBlank String moneda,
    String observaciones,
    Boolean reservaInventario,
    @Valid @NotEmpty List<PreordenItemRequest> items
) {
}
