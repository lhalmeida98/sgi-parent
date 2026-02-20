package ec.sgi.backend.application.port.in;

import ec.sgi.backend.domain.model.UsuarioEmpresa;
import java.util.List;
import java.util.Objects;

public record ActualizarUsuarioCommand(
    String nombre,
    String usuario,
    String email,
    List<String> roles,
    List<UsuarioEmpresa> empresas,
    Boolean activo,
    String password
) {
  public ActualizarUsuarioCommand {
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(usuario, "usuario");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(roles, "roles");
    Objects.requireNonNull(empresas, "empresas");
    Objects.requireNonNull(activo, "activo");
  }
}
