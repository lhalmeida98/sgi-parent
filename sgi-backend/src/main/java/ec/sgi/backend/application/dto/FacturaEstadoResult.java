package ec.sgi.backend.application.dto;


public record FacturaEstadoResult(
    Long facturaId,
    String estado,
    String claveAcceso,
    String coreComprobanteId,
    SriEstadoDto sriEstado
) {
}
