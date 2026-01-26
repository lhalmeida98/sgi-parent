package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AccionCreateRequest(
    @NotBlank String codigo,
    String descripcion,
    Boolean activo
) {
}
