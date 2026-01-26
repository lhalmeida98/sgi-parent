package ec.sgi.backend.application.dto;

public record AuthLoginResult(
    String token,
    String tipo,
    String rol,
    Long empresaId
) {
}
