package ec.sgi.backend.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record Bodega(
    Long id,
    Long empresaId,
    String nombre,
    String descripcion,
    String direccion,
    boolean activa,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
  public Bodega {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(nombre, "nombre");
  }
}
