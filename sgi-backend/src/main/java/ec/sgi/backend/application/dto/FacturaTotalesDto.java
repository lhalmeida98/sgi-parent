package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record FacturaTotalesDto(
    BigDecimal totalSinImpuestos,
    BigDecimal totalDescuento,
    BigDecimal totalImpuestos,
    BigDecimal importeTotal
) {
}
