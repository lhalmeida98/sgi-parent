package ec.sgi.backend.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RolResult(
    Long id,
    String nombre,
    String descripcion,
    boolean activo,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn,
    List<Long> accionesIds
) {
}
