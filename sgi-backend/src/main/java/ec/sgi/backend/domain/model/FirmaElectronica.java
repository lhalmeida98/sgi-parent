package ec.sgi.backend.domain.model;

import java.util.Objects;

public record FirmaElectronica(
    Long id,
    Long empresaId,
    String nombreArchivo,
    String tipoContenido,
    String rutaArchivo,
    String clave
) {
  public FirmaElectronica {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(nombreArchivo, "nombreArchivo");
    Objects.requireNonNull(rutaArchivo, "rutaArchivo");
    Objects.requireNonNull(clave, "clave");
  }
}
