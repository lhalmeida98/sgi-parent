package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record ActualizarCategoriaCommand(
    String nombre,
    String descripcion
) {
  public ActualizarCategoriaCommand {
    Objects.requireNonNull(nombre, "nombre");
  }
}
