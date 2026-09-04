package ec.sri.einvoice.infrastructure.xml;

import ec.sri.einvoice.application.port.out.XmlComprobanteGenerator;
import ec.sri.einvoice.domain.model.CampoAdicional;
import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.domain.model.Detalle;
import ec.sri.einvoice.domain.model.InfoDocumento;
import ec.sri.einvoice.domain.model.InfoFactura;
import ec.sri.einvoice.domain.model.InfoTributaria;
import ec.sri.einvoice.domain.model.Impuesto;
import ec.sri.einvoice.domain.model.Pago;
import ec.sri.einvoice.domain.model.TipoComprobante;
import ec.sri.einvoice.domain.model.TotalImpuesto;
import ec.sri.einvoice.infrastructure.config.AppProperties;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class SimpleXmlComprobanteGenerator implements XmlComprobanteGenerator {
  private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private final AppProperties properties;

  public SimpleXmlComprobanteGenerator(AppProperties properties) {
    this.properties = properties;
  }

  @Override
  public String generar(Comprobante comprobante) {
    if (comprobante.tipo() != TipoComprobante.FACTURA) {
      throw new IllegalArgumentException("Tipo de comprobante no soportado en XML: " + comprobante.tipo());
    }

    InfoTributaria infoTributaria = comprobante.infoTributaria();
    InfoDocumento infoDocumento = comprobante.infoDocumento();
    InfoFactura infoFactura = (InfoFactura) infoDocumento;

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version='1.0' encoding='UTF-8'?>");
    String version = properties.getSri().getXmlVersion();
    xml.append("<factura id=\"comprobante\" version=\"").append(escapeAttribute(version)).append("\">");
    xml.append("<infoTributaria>");
    xml.append(tag("ambiente", infoTributaria.ambiente().codigo()));
    xml.append(tag("tipoEmision", infoTributaria.tipoEmision().codigo()));
    xml.append(tag("razonSocial", escape(infoTributaria.razonSocial())));
    xml.append(tag("nombreComercial", escape(infoTributaria.nombreComercial())));
    xml.append(tag("ruc", infoTributaria.ruc()));
    xml.append(tag("claveAcceso", infoTributaria.claveAcceso().value()));
    xml.append(tag("codDoc", comprobante.tipo().codigo()));
    xml.append(tag("estab", infoTributaria.estab()));
    xml.append(tag("ptoEmi", infoTributaria.ptoEmi()));
    xml.append(tag("secuencial", infoTributaria.secuencial()));
    xml.append(tag("dirMatriz", escape(infoTributaria.dirMatriz())));
    xml.append(optionalTag("agenteRetencion", infoTributaria.agenteRetencion()));
    xml.append(optionalEscapedTag("contribuyenteRimpe", infoTributaria.contribuyenteRimpe()));
    xml.append("</infoTributaria>");

    xml.append("<infoFactura>");
    xml.append(tag("fechaEmision", infoFactura.fechaEmision().format(FECHA_FORMATO)));
    xml.append(tag("dirEstablecimiento", escape(infoFactura.dirEstablecimiento())));
    xml.append(optionalTag("contribuyenteEspecial", infoFactura.contribuyenteEspecial()));
    xml.append(optionalTag("obligadoContabilidad", infoFactura.obligadoContabilidad()));
    xml.append(tag("tipoIdentificacionComprador", infoFactura.tipoIdentificacionComprador().codigo()));
    xml.append(tag("razonSocialComprador", escape(infoFactura.razonSocialComprador())));
    xml.append(tag("identificacionComprador", infoFactura.identificacionComprador()));
    xml.append(optionalEscapedTag("direccionComprador", infoFactura.direccionComprador()));
    xml.append(tag("totalSinImpuestos", infoFactura.totalSinImpuestos().toPlainString()));
    xml.append(tag("totalDescuento", infoFactura.totalDescuento().toPlainString()));
    xml.append("<totalConImpuestos>");
    for (TotalImpuesto total : infoFactura.totalConImpuestos()) {
      xml.append("<totalImpuesto>");
      xml.append(tag("codigo", total.codigo()));
      xml.append(tag("codigoPorcentaje", total.codigoPorcentaje()));
      xml.append(tag("baseImponible", total.baseImponible().toPlainString()));
      xml.append(tag("valor", total.valor().toPlainString()));
      xml.append("</totalImpuesto>");
    }
    xml.append("</totalConImpuestos>");
    xml.append(tag("propina", infoFactura.propina().toPlainString()));
    xml.append(tag("importeTotal", infoFactura.importeTotal().toPlainString()));
    xml.append(tag("moneda", infoFactura.moneda()));
    if (!infoFactura.pagos().isEmpty()) {
      xml.append("<pagos>");
      for (Pago pago : infoFactura.pagos()) {
        xml.append("<pago>");
        xml.append(tag("formaPago", pago.formaPago()));
        xml.append(tag("total", pago.total().toPlainString()));
        xml.append(optionalTag("plazo", pago.plazo() == null ? null : pago.plazo().toString()));
        xml.append(optionalEscapedTag("unidadTiempo", pago.unidadTiempo()));
        xml.append("</pago>");
      }
      xml.append("</pagos>");
    }
    xml.append("</infoFactura>");

    xml.append("<detalles>");
    for (Detalle detalle : comprobante.detalles()) {
      xml.append("<detalle>");
      xml.append(tag("codigoPrincipal", escape(detalle.codigoPrincipal())));
      xml.append(tag("descripcion", escape(detalle.descripcion())));
      xml.append(tag("cantidad", detalle.cantidad().toPlainString()));
      xml.append(tag("precioUnitario", detalle.precioUnitario().toPlainString()));
      xml.append(tag("descuento", detalle.descuento().toPlainString()));
      xml.append(tag("precioTotalSinImpuesto", detalle.precioTotalSinImpuesto().toPlainString()));
      xml.append("<impuestos>");
      for (Impuesto impuesto : detalle.impuestos()) {
        xml.append("<impuesto>");
        xml.append(tag("codigo", impuesto.codigo()));
        xml.append(tag("codigoPorcentaje", impuesto.codigoPorcentaje()));
        xml.append(tag("tarifa", impuesto.tarifa().toPlainString()));
        xml.append(tag("baseImponible", impuesto.baseImponible().toPlainString()));
        xml.append(tag("valor", impuesto.valor().toPlainString()));
        xml.append("</impuesto>");
      }
      xml.append("</impuestos>");
      xml.append("</detalle>");
    }
    xml.append("</detalles>");
    if (!comprobante.infoAdicional().isEmpty()) {
      xml.append("<infoAdicional>");
      for (CampoAdicional campo : comprobante.infoAdicional()) {
        xml.append("<campoAdicional nombre=\"")
            .append(escapeAttribute(campo.nombre()))
            .append("\">")
            .append(escape(campo.valor()))
            .append("</campoAdicional>");
      }
      xml.append("</infoAdicional>");
    }
    xml.append("</factura>");
    return xml.toString();
  }

  private String tag(String name, String value) {
    return "<" + name + ">" + value + "</" + name + ">";
  }

  private String optionalTag(String name, String value) {
    if (value == null || value.isBlank()) {
      return "";
    }
    return tag(name, value);
  }

  private String optionalEscapedTag(String name, String value) {
    if (value == null || value.isBlank()) {
      return "";
    }
    return tag(name, escape(value));
  }

  private String escape(String value) {
    return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  private String escapeAttribute(String value) {
    return escape(value).replace("\"", "&quot;");
  }
}
