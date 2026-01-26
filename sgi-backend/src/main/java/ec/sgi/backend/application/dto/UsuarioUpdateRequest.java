package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioUpdateRequest(
    @NotBlank String nombre,
    @Email @NotBlank String email,
    @NotBlank String rol,
    @NotNull Boolean activo,
    String password
) {
}
