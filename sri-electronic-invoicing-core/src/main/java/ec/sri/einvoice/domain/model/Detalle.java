package ec.sri.einvoice.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class Detalle {
  private final String codigoPrincipal;
  private final String descripcion;
  private final BigDecimal cantidad;
  private final BigDecimal precioUnitario;
  private final BigDecimal descuento;
  private final BigDecimal precioTotalSinImpuesto;
  private final List<Impuesto> impuestos;

  public Detalle(
      String codigoPrincipal,
      String descripcion,
      BigDecimal cantidad,
      BigDecimal precioUnitario,
      BigDecimal descuento,
      BigDecimal precioTotalSinImpuesto,
      List<Impuesto> impuestos
  ) {
    this.codigoPrincipal = Objects.requireNonNull(codigoPrincipal, "codigoPrincipal");
    this.descripcion = Objects.requireNonNull(descripcion, "descripcion");
    this.cantidad = Objects.requireNonNull(cantidad, "cantidad");
    this.precioUnitario = Objects.requireNonNull(precioUnitario, "precioUnitario");
    this.descuento = Objects.requireNonNull(descuento, "descuento");
    this.precioTotalSinImpuesto = Objects.requireNonNull(precioTotalSinImpuesto, "precioTotalSinImpuesto");
    this.impuestos = List.copyOf(impuestos);
  }

  public String codigoPrincipal() {
    return codigoPrincipal;
  }

  public String descripcion() {
    return descripcion;
  }

  public BigDecimal cantidad() {
    return cantidad;
  }

  public BigDecimal precioUnitario() {
    return precioUnitario;
  }

  public BigDecimal descuento() {
    return descuento;
  }

  public BigDecimal precioTotalSinImpuesto() {
    return precioTotalSinImpuesto;
  }

  public List<Impuesto> impuestos() {
    return impuestos;
  }
}
