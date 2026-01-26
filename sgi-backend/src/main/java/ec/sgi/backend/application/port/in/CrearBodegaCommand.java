package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record CrearBodegaCommand(
    Long empresaId,
    String nombre,
    String descripcion,
    String direccion,
    Boolean activa
) {
  public CrearBodegaCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(nombre, "nombre");
  }
}
