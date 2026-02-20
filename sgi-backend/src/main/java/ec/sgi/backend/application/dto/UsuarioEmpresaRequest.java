package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotNull;

public record UsuarioEmpresaRequest(
    @NotNull Long empresaId,
    Boolean principal
) {
}
