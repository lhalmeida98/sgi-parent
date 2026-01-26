package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record ActualizarUsuarioCommand(
    String nombre,
    String email,
    String rol,
    Boolean activo,
    String password
) {
  public ActualizarUsuarioCommand {
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(rol, "rol");
    Objects.requireNonNull(activo, "activo");
  }
}
