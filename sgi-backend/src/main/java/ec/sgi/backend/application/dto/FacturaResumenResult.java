package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FacturaResumenResult(
    Long id,
    Long empresaId,
    Long clienteId,
    String clienteRazonSocial,
    LocalDate fechaEmision,
    String numeroFactura,
    String estado,
    String claveAcceso,
    String numeroAutorizacion,
    LocalDateTime fechaAutorizacion,
    SriEstadoDto sriEstado,
    BigDecimal totalSinImpuestos,
    BigDecimal totalDescuento,
    BigDecimal totalImpuestos,
    BigDecimal importeTotal
) {
}
