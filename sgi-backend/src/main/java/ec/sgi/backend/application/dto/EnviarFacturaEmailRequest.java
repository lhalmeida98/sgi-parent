package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotNull;

public record EnviarFacturaEmailRequest(
    @NotNull Long facturaId,
    String subject
) {
}
