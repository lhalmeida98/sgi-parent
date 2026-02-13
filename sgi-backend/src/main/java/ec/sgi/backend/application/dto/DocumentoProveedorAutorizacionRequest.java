package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentoProveedorAutorizacionRequest(
    @NotBlank String numeroAutorizacion
) {
}
