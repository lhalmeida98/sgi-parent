package ec.sgi.backend.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record Rol(
    Long id,
    String nombre,
    String descripcion,
    boolean activo,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn,
    List<Long> accionesIds
) {
  public Rol {
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(accionesIds, "accionesIds");
  }
}
