package ec.sgi.backend.application.port.in;

import java.util.List;
import java.util.Objects;

public record CrearRolCommand(
    String nombre,
    String descripcion,
    List<Long> accionesIds,
    Boolean activo
) {
  public CrearRolCommand {
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(accionesIds, "accionesIds");
  }
}
