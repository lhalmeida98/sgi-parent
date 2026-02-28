package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record ActualizarClienteCommand(
    String tipoIdentificacion,
    String identificacion,
    String razonSocial,
    String email,
    String direccion,
    Integer creditoDias
) {
  public ActualizarClienteCommand {
    Objects.requireNonNull(tipoIdentificacion, "tipoIdentificacion");
    Objects.requireNonNull(identificacion, "identificacion");
    Objects.requireNonNull(razonSocial, "razonSocial");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(direccion, "direccion");
  }
}
