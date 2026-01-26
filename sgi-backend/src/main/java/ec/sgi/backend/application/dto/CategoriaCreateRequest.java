package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaCreateRequest(
    @NotBlank String nombre,
    String descripcion
) {
}
