package ec.sgi.backend.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record Accion(
    Long id,
    Long empresaId,
    String codigo,
    String descripcion,
    boolean activo,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
  public Accion {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(codigo, "codigo");
  }
}
