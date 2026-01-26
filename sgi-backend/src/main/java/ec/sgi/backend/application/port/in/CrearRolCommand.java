package ec.sgi.backend.application.port.in;

import java.util.List;
import java.util.Objects;

public record CrearRolCommand(
    Long empresaId,
    String nombre,
    String descripcion,
    List<String> permisos
) {
  public CrearRolCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(permisos, "permisos");
  }
}
