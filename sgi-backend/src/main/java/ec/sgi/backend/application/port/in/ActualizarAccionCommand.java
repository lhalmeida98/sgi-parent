package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record ActualizarAccionCommand(
    String nombre,
    String codigo,
    String descripcion,
    String url,
    String icono,
    String tipo,
    Boolean activo
) {
  public ActualizarAccionCommand {
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(codigo, "codigo");
    Objects.requireNonNull(activo, "activo");
  }
}
