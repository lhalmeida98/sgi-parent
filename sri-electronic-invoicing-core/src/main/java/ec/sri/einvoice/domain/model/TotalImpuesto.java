package ec.sri.einvoice.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class TotalImpuesto {
  private final String codigo;
  private final String codigoPorcentaje;
  private final BigDecimal baseImponible;
  private final BigDecimal valor;

  public TotalImpuesto(
      String codigo,
      String codigoPorcentaje,
      BigDecimal baseImponible,
      BigDecimal valor
  ) {
    this.codigo = Objects.requireNonNull(codigo, "codigo");
    this.codigoPorcentaje = Objects.requireNonNull(codigoPorcentaje, "codigoPorcentaje");
    this.baseImponible = Objects.requireNonNull(baseImponible, "baseImponible");
    this.valor = Objects.requireNonNull(valor, "valor");
  }

  public String codigo() {
    return codigo;
  }

  public String codigoPorcentaje() {
    return codigoPorcentaje;
  }

  public BigDecimal baseImponible() {
    return baseImponible;
  }

  public BigDecimal valor() {
    return valor;
  }
}
