package ec.sgi.backend.application.dto;

public record ProveedorResult(
    Long id,
    String tipoIdentificacion,
    String identificacion,
    String razonSocial,
    String nombreComercial,
    String email,
    String telefono,
    String direccion,
    String condicionesPago,
    String estado
) {
}
