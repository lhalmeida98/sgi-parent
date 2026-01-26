package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record SriTotalImpuestoDto(
    String codigo,
    String codigoPorcentaje,
    BigDecimal baseImponible,
    BigDecimal valor
) {
}
