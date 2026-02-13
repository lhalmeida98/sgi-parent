package ec.sgi.backend.application.port.in;

public record ActualizarProveedorCommand(
    String razonSocial,
    String nombreComercial,
    String email,
    String telefono,
    String direccion,
    String condicionesPago,
    boolean activo
) {
}
