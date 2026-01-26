package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record FacturaImpuesto(
    String codigo,
    String codigoPorcentaje,
    BigDecimal tarifa,
    BigDecimal baseImponible,
    BigDecimal valor
) {
  public FacturaImpuesto {
    Objects.requireNonNull(codigo, "codigo");
    Objects.requireNonNull(codigoPorcentaje, "codigoPorcentaje");
    Objects.requireNonNull(tarifa, "tarifa");
    Objects.requireNonNull(baseImponible, "baseImponible");
    Objects.requireNonNull(valor, "valor");
  }
}
