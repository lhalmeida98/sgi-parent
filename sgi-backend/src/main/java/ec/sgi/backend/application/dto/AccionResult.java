package ec.sgi.backend.application.dto;

import java.time.LocalDateTime;

public record AccionResult(
    Long id,
    String codigo,
    String descripcion,
    boolean activo,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
}
