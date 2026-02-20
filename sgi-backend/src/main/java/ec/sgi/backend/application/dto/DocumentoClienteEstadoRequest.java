package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentoClienteEstadoRequest(
    @NotBlank String estado,
    String motivo
) {
}
