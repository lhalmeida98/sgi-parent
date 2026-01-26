package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
    @NotBlank String email,
    @NotBlank String password
) {
}
