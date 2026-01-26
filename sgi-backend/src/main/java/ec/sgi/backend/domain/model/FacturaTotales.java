package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record FacturaTotales(
    BigDecimal totalSinImpuestos,
    BigDecimal totalDescuento,
    BigDecimal totalImpuestos,
    BigDecimal importeTotal
) {
  public FacturaTotales {
    Objects.requireNonNull(totalSinImpuestos, "totalSinImpuestos");
    Objects.requireNonNull(totalDescuento, "totalDescuento");
    Objects.requireNonNull(totalImpuestos, "totalImpuestos");
    Objects.requireNonNull(importeTotal, "importeTotal");
  }
}
