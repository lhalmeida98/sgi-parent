package ec.sri.einvoice.domain;

import ec.sri.einvoice.domain.model.Ambiente;
import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.domain.model.ComprobanteId;
import ec.sri.einvoice.domain.model.Detalle;
import ec.sri.einvoice.domain.model.Impuesto;
import ec.sri.einvoice.domain.model.InfoFactura;
import ec.sri.einvoice.domain.model.InfoTributaria;
import ec.sri.einvoice.domain.model.TipoComprobante;
import ec.sri.einvoice.domain.model.TipoEmision;
import ec.sri.einvoice.domain.model.TipoIdentificacion;
import ec.sri.einvoice.domain.model.TotalImpuesto;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ComprobanteTestData {
  private ComprobanteTestData() {
  }

  public static Comprobante facturaValida() {
    InfoTributaria infoTributaria = new InfoTributaria(
        Ambiente.PRUEBAS,
        TipoEmision.NORMAL,
        "Empresa Demo",
        "Empresa Demo",
        "1790012345001",
        "Quito",
        "001",
        "002",
        "000000123",
        null,
        null,
        null
    );

    TotalImpuesto totalImpuesto = new TotalImpuesto(
        "2",
        "2",
        new BigDecimal("100.00"),
        new BigDecimal("12.00")
    );

    InfoFactura infoFactura = new InfoFactura(
        LocalDate.of(2024, 1, 15),
        "Quito",
        TipoIdentificacion.CEDULA,
        "Cliente Demo",
        "0912345678",
        new BigDecimal("100.00"),
        new BigDecimal("0.00"),
        new BigDecimal("0.00"),
        new BigDecimal("112.00"),
        "USD",
        List.of(totalImpuesto)
    );

    Impuesto impuesto = new Impuesto(
        "2",
        "2",
        new BigDecimal("12"),
        new BigDecimal("100.00"),
        new BigDecimal("12.00")
    );

    Detalle detalle = new Detalle(
        "SKU-01",
        "Producto",
        new BigDecimal("1"),
        new BigDecimal("100.00"),
        new BigDecimal("0.00"),
        new BigDecimal("100.00"),
        List.of(impuesto)
    );

    return Comprobante.crear(
        ComprobanteId.newId(),
        TipoComprobante.FACTURA,
        infoTributaria,
        infoFactura,
        List.of(detalle),
        Instant.parse("2024-01-15T10:00:00Z")
    );
  }
}
