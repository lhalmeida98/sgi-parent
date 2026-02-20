package ec.sgi.backend.application.dto;

import java.time.LocalDateTime;

public record AccionResult(
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
}
