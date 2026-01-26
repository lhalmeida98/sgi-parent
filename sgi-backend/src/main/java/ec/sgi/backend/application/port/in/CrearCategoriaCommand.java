package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record CrearCategoriaCommand(
    Long empresaId,
    String nombre,
    String descripcion
) {
  public CrearCategoriaCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(nombre, "nombre");
  }
}
