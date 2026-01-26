package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record FacturaImpuestoTotal(
    String codigo,
    String codigoPorcentaje,
    BigDecimal baseImponible,
    BigDecimal valor
) {
  public FacturaImpuestoTotal {
    Objects.requireNonNull(codigo, "codigo");
    Objects.requireNonNull(codigoPorcentaje, "codigoPorcentaje");
    Objects.requireNonNull(baseImponible, "baseImponible");
    Objects.requireNonNull(valor, "valor");
  }
}
