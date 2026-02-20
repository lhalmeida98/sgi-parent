package ec.sgi.backend.application.dto;

import java.util.List;

public record AuthLoginResult(
    String token,
    String tipo,
    List<String> roles,
    List<AccionMenuResult> acciones,
    Long empresaId
) {
}
