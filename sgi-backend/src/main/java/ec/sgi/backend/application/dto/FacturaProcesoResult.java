package ec.sgi.backend.application.dto;

import java.time.LocalDateTime;

public record FacturaProcesoResult(
    Long facturaId,
    String estado,
    String claveAcceso,
    String coreComprobanteId,
    int intentosConsulta,
    LocalDateTime ultimoIntentoConsulta,
    String numeroAutorizacion,
    SriEstadoDto sriEstado
) {
}
