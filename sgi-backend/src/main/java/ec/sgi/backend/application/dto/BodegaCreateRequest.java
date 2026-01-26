package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record BodegaCreateRequest(
    @NotBlank String nombre,
    String descripcion,
    String direccion,
    Boolean activa
) {
}
