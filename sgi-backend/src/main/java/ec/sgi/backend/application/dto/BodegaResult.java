package ec.sgi.backend.application.dto;

import java.time.LocalDateTime;

public record BodegaResult(
    Long id,
    String nombre,
    String descripcion,
    String direccion,
    boolean activa,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
}
