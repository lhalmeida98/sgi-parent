package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record CrearAccionCommand(
    Long empresaId,
    String codigo,
    String descripcion,
    Boolean activo
) {
  public CrearAccionCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(codigo, "codigo");
  }
}
