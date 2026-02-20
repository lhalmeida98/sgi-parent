package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccionUpdateRequest(
    @NotBlank String nombre,
    @NotBlank String codigo,
    String descripcion,
    String url,
    String icono,
    String tipo,
    @NotNull Boolean activo
) {
}
