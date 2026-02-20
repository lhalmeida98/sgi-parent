package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AccionCreateRequest(
    @NotBlank String nombre,
    @NotBlank String codigo,
    String descripcion,
    String url,
    String icono,
    String tipo,
    Boolean activo
) {
}
