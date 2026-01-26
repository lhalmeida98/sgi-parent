package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RolCreateRequest(
    @NotBlank String nombre,
    String descripcion,
    @NotNull List<String> permisos
) {
}
