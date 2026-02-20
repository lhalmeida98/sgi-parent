package ec.sgi.backend.application.port.in;

public record CrearClienteCommand(
    Long empresaId,
    String tipoIdentificacion,
    String identificacion,
    String razonSocial,
    String email,
    String direccion,
    Integer creditoDias
) {
  public CrearClienteCommand {
    java.util.Objects.requireNonNull(empresaId, "empresaId");
    java.util.Objects.requireNonNull(tipoIdentificacion, "tipoIdentificacion");
    java.util.Objects.requireNonNull(identificacion, "identificacion");
    java.util.Objects.requireNonNull(razonSocial, "razonSocial");
    java.util.Objects.requireNonNull(email, "email");
    java.util.Objects.requireNonNull(direccion, "direccion");
  }
}
