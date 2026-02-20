package ec.sgi.backend.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioResult(
    Long id,
    String nombre,
    String usuario,
    String email,
    List<String> roles,
    List<UsuarioEmpresaResult> empresas,
    boolean activo,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
}
