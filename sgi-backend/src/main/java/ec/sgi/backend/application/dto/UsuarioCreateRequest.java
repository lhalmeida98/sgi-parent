package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioCreateRequest(
    @NotBlank String nombre,
    @NotBlank String usuario,
    @Email @NotBlank String email,
    @NotBlank String password,
    @NotBlank String rol,
    Boolean activo
) {
}
