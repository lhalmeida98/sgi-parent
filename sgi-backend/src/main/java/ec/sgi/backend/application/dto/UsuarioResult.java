package ec.sgi.backend.application.dto;

import java.time.LocalDateTime;

public record UsuarioResult(
    Long id,
    String nombre,
    String usuario,
    String email,
    String rol,
    boolean activo,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
}
