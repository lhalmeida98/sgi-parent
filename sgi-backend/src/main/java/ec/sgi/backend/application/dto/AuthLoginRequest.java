package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
    String usuario,
    String email,
    @NotBlank String password
) {
}
