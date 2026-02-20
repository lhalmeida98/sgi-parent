package ec.sgi.backend.application.dto;

public record AccionMenuResult(
    String nombre,
    String descripcion,
    String url,
    String icono,
    String tipo
) {
}
