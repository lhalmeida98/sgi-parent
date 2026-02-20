package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record CrearAccionCommand(
    String nombre,
    String codigo,
    String descripcion,
    String url,
    String icono,
    String tipo,
    Boolean activo
) {
  public CrearAccionCommand {
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(codigo, "codigo");
  }
}
