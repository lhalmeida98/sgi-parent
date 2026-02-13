package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProveedorCreateRequest(
    @NotBlank String tipoIdentificacion,
    @NotBlank String identificacion,
    @NotBlank String razonSocial,
    String nombreComercial,
    @Email String email,
    String telefono,
    String direccion,
    @NotBlank String condicionesPago,
    @NotNull Boolean activo
) {
}
