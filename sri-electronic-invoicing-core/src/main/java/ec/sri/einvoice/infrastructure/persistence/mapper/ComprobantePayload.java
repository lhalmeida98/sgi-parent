package ec.sri.einvoice.infrastructure.persistence.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ComprobantePayload(
    String tipo,
    InfoTributariaPayload infoTributaria,
    InfoFacturaPayload infoFactura,
    List<DetallePayload> detalles
) {
  public record InfoTributariaPayload(
      String ambiente,
      String tipoEmision,
      String razonSocial,
      String nombreComercial,
      String ruc,
      String dirMatriz,
      String estab,
      String ptoEmi,
      String secuencial,
      String claveAcceso,
      String firmaElectronica,
      String claveFirma
  ) {
  }

  public record InfoFacturaPayload(
      LocalDate fechaEmision,
      String dirEstablecimiento,
      String tipoIdentificacionComprador,
      String razonSocialComprador,
      String identificacionComprador,
      BigDecimal totalSinImpuestos,
      BigDecimal totalDescuento,
      BigDecimal propina,
      BigDecimal importeTotal,
      String moneda,
      List<TotalImpuestoPayload> totalConImpuestos
  ) {
  }

  public record DetallePayload(
      String codigoPrincipal,
      String descripcion,
      BigDecimal cantidad,
      BigDecimal precioUnitario,
      BigDecimal descuento,
      BigDecimal precioTotalSinImpuesto,
      List<ImpuestoPayload> impuestos
  ) {
  }

  public record ImpuestoPayload(
      String codigo,
      String codigoPorcentaje,
      BigDecimal tarifa,
      BigDecimal baseImponible,
      BigDecimal valor
  ) {
  }

  public record TotalImpuestoPayload(
      String codigo,
      String codigoPorcentaje,
      BigDecimal baseImponible,
      BigDecimal valor
  ) {
  }
}
