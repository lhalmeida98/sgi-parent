package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record SriImpuestoDto(
    String codigo,
    String codigoPorcentaje,
    BigDecimal tarifa,
    BigDecimal baseImponible,
    BigDecimal valor
) {
}
