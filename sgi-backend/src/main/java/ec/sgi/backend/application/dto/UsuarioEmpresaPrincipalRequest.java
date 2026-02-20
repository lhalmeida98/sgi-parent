package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotNull;

public record UsuarioEmpresaPrincipalRequest(
    @NotNull Long empresaId
) {
}
