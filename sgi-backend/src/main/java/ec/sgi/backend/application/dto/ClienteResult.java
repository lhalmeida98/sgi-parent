package ec.sgi.backend.application.dto;


public record ClienteResult(
    Long id,
    String tipoIdentificacion,
    String identificacion,
    String razonSocial,
    String email,
    String direccion,
    Integer creditoDias
) {
}
