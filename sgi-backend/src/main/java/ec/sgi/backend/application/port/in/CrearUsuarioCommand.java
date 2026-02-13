package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record CrearUsuarioCommand(
    Long empresaId,
    String nombre,
    String usuario,
    String email,
    String password,
    String rol,
    Boolean activo
) {
  public CrearUsuarioCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(usuario, "usuario");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(password, "password");
    Objects.requireNonNull(rol, "rol");
  }
}
