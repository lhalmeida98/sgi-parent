package ec.sgi.backend.application.dto;

public record SriEmitirFacturaResult(
    String comprobanteId,
    String claveAcceso,
    SriEnvioStatus estadoSri,
    String mensajeSri,
    String numeroAutorizacion
) {
}
