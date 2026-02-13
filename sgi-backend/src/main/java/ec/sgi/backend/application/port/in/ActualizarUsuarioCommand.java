package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record ActualizarUsuarioCommand(
    String nombre,
    String usuario,
    String email,
    String rol,
    Boolean activo,
    String password
) {
  public ActualizarUsuarioCommand {
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(usuario, "usuario");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(rol, "rol");
    Objects.requireNonNull(activo, "activo");
  }
}
