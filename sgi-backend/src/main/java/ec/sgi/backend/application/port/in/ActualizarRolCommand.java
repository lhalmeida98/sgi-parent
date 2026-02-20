package ec.sgi.backend.application.port.in;

import java.util.List;
import java.util.Objects;

public record ActualizarRolCommand(
    String nombre,
    String descripcion,
    List<Long> accionesIds,
    Boolean activo
) {
  public ActualizarRolCommand {
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(accionesIds, "accionesIds");
    Objects.requireNonNull(activo, "activo");
  }
}
