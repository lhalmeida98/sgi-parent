package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClienteUpdateRequest(
    @NotBlank String tipoIdentificacion,
    @NotBlank String identificacion,
    @NotBlank String razonSocial,
    @NotBlank @Email String email,
    @NotBlank String direccion,
    Integer creditoDias
) {
}
