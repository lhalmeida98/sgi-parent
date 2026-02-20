package ec.sgi.backend.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record Accion(
    Long id,
    String nombre,
    String codigo,
    String descripcion,
    String url,
    String icono,
    String tipo,
    boolean activo,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
  public Accion {
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(codigo, "codigo");
  }
}
