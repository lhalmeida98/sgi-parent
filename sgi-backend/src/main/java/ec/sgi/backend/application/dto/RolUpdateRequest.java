package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RolUpdateRequest(
    @NotBlank String nombre,
    String descripcion,
    @NotNull List<Long> accionesIds,
    @NotNull Boolean activo
) {
}
