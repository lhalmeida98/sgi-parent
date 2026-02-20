package ec.sgi.backend.application.port.in;

import ec.sgi.backend.domain.model.UsuarioEmpresa;
import java.util.List;
import java.util.Objects;

public record CrearUsuarioCommand(
    String nombre,
    String usuario,
    String email,
    String password,
    List<String> roles,
    List<UsuarioEmpresa> empresas,
    Boolean activo
) {
  public CrearUsuarioCommand {
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(usuario, "usuario");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(password, "password");
    Objects.requireNonNull(roles, "roles");
    Objects.requireNonNull(empresas, "empresas");
  }
}
