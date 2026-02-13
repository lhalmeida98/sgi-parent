package ec.sgi.backend.application.port.in;

public record CrearProveedorCommand(
    Long empresaId,
    String tipoIdentificacion,
    String identificacion,
    String razonSocial,
    String nombreComercial,
    String email,
    String telefono,
    String direccion,
    String condicionesPago,
    boolean activo
) {
}
