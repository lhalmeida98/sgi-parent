package ec.sgi.backend.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record Proveedor(
    Long id,
    Long empresaId,
    String tipoIdentificacion,
    String identificacion,
    String razonSocial,
    String nombreComercial,
    String email,
    String telefono,
    String direccion,
    String condicionesPago,
    String estado,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
  public Proveedor {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(tipoIdentificacion, "tipoIdentificacion");
    Objects.requireNonNull(identificacion, "identificacion");
    Objects.requireNonNull(razonSocial, "razonSocial");
    Objects.requireNonNull(condicionesPago, "condicionesPago");
    Objects.requireNonNull(estado, "estado");
  }
}
