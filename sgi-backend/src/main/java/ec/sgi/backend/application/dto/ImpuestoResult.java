package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record ImpuestoResult(
    Long id,
    String codigo,
    String codigoPorcentaje,
    BigDecimal tarifa,
    String descripcion,
    boolean activo
) {
}
