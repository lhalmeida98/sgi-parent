package ec.sgi.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioCreateRequest(
    @NotBlank String nombre,
    @NotBlank String usuario,
    @Email @NotBlank String email,
    @NotBlank String password,
    @NotNull List<String> roles,
    @NotNull List<UsuarioEmpresaRequest> empresas,
    Boolean activo
) {
}
