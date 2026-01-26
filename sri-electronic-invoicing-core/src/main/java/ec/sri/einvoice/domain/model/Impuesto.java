package ec.sri.einvoice.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Impuesto {
  private final String codigo;
  private final String codigoPorcentaje;
  private final BigDecimal tarifa;
  private final BigDecimal baseImponible;
  private final BigDecimal valor;

  public Impuesto(
      String codigo,
      String codigoPorcentaje,
      BigDecimal tarifa,
      BigDecimal baseImponible,
      BigDecimal valor
  ) {
    this.codigo = Objects.requireNonNull(codigo, "codigo");
    this.codigoPorcentaje = Objects.requireNonNull(codigoPorcentaje, "codigoPorcentaje");
    this.tarifa = Objects.requireNonNull(tarifa, "tarifa");
    this.baseImponible = Objects.requireNonNull(baseImponible, "baseImponible");
    this.valor = Objects.requireNonNull(valor, "valor");
  }

  public String codigo() {
    return codigo;
  }

  public String codigoPorcentaje() {
    return codigoPorcentaje;
  }

  public BigDecimal tarifa() {
    return tarifa;
  }

  public BigDecimal baseImponible() {
    return baseImponible;
  }

  public BigDecimal valor() {
    return valor;
  }
}
