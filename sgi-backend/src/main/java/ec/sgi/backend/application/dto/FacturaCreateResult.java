package ec.sgi.backend.application.dto;


public record FacturaCreateResult(
    Long facturaId,
    String estado,
    String claveAcceso,
    String coreComprobanteId,
    FacturaTotalesDto totales,
    SriEstadoDto sriEstado
) {
}
