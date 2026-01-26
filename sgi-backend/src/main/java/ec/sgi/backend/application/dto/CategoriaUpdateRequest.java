package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaUpdateRequest(
    @NotBlank String nombre,
    String descripcion
) {
}
