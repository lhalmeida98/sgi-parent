package ec.sgi.backend.domain.model;

import java.util.Objects;

public record Cliente(
    Long id,
    Long empresaId,
    String tipoIdentificacion,
    String identificacion,
    String razonSocial,
    String email,
    String direccion
) {
  public Cliente {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(tipoIdentificacion, "tipoIdentificacion");
    Objects.requireNonNull(identificacion, "identificacion");
    Objects.requireNonNull(razonSocial, "razonSocial");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(direccion, "direccion");
  }
}
