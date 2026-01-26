package ec.sgi.backend.application.dto;

import java.util.List;

public record RolResult(
    Long id,
    String nombre,
    String descripcion,
    List<String> permisos
) {
}
