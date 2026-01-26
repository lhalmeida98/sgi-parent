package ec.sgi.backend.domain.model;

import java.util.List;
import java.util.Objects;

public record Rol(
    Long id,
    Long empresaId,
    String nombre,
    String descripcion,
    List<String> permisos
) {
  public Rol {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(permisos, "permisos");
  }
}
