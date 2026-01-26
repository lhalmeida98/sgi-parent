package ec.sgi.backend.domain.model;

import java.util.Objects;

public record Categoria(
    Long id,
    Long empresaId,
    String nombre,
    String descripcion
) {
  public Categoria {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(nombre, "nombre");
  }
}
