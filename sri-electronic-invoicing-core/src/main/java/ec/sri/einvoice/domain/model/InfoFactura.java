package ec.sri.einvoice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class InfoFactura implements InfoDocumento {
  private final LocalDate fechaEmision;
  private final String dirEstablecimiento;
  private final TipoIdentificacion tipoIdentificacionComprador;
  private final String razonSocialComprador;
  private final String identificacionComprador;
  private final BigDecimal totalSinImpuestos;
  private final BigDecimal totalDescuento;
  private final BigDecimal propina;
  private final BigDecimal importeTotal;
  private final String moneda;
  private final List<TotalImpuesto> totalConImpuestos;

  public InfoFactura(
      LocalDate fechaEmision,
      String dirEstablecimiento,
      TipoIdentificacion tipoIdentificacionComprador,
      String razonSocialComprador,
      String identificacionComprador,
      BigDecimal totalSinImpuestos,
      BigDecimal totalDescuento,
      BigDecimal propina,
      BigDecimal importeTotal,
      String moneda,
      List<TotalImpuesto> totalConImpuestos
  ) {
    this.fechaEmision = Objects.requireNonNull(fechaEmision, "fechaEmision");
    this.dirEstablecimiento = Objects.requireNonNull(dirEstablecimiento, "dirEstablecimiento");
    this.tipoIdentificacionComprador = Objects.requireNonNull(tipoIdentificacionComprador, "tipoIdentificacionComprador");
    this.razonSocialComprador = Objects.requireNonNull(razonSocialComprador, "razonSocialComprador");
    this.identificacionComprador = Objects.requireNonNull(identificacionComprador, "identificacionComprador");
    this.totalSinImpuestos = Objects.requireNonNull(totalSinImpuestos, "totalSinImpuestos");
    this.totalDescuento = Objects.requireNonNull(totalDescuento, "totalDescuento");
    this.propina = Objects.requireNonNull(propina, "propina");
    this.importeTotal = Objects.requireNonNull(importeTotal, "importeTotal");
    this.moneda = Objects.requireNonNull(moneda, "moneda");
    this.totalConImpuestos = List.copyOf(totalConImpuestos);
  }

  @Override
  public TipoComprobante tipo() {
    return TipoComprobante.FACTURA;
  }

  public LocalDate fechaEmision() {
    return fechaEmision;
  }

  public String dirEstablecimiento() {
    return dirEstablecimiento;
  }

  public TipoIdentificacion tipoIdentificacionComprador() {
    return tipoIdentificacionComprador;
  }

  public String razonSocialComprador() {
    return razonSocialComprador;
  }

  public String identificacionComprador() {
    return identificacionComprador;
  }

  public BigDecimal totalSinImpuestos() {
    return totalSinImpuestos;
  }

  public BigDecimal totalDescuento() {
    return totalDescuento;
  }

  public BigDecimal propina() {
    return propina;
  }

  public BigDecimal importeTotal() {
    return importeTotal;
  }

  public String moneda() {
    return moneda;
  }

  public List<TotalImpuesto> totalConImpuestos() {
    return totalConImpuestos;
  }
}
