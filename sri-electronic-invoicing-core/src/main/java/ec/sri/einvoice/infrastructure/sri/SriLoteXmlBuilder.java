package ec.sri.einvoice.infrastructure.sri;

import ec.sri.einvoice.domain.model.ClaveAcceso;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class SriLoteXmlBuilder {
  private static final int MAX_COMPROBANTES = 50;
  private static final int MAX_BYTES = 512 * 1024;

  String build(String claveAccesoLote, String rucEmisor, List<String> xmlsFirmados) {
    if (isBlank(claveAccesoLote)) {
      throw new IllegalArgumentException("Clave de acceso de lote requerida");
    }
    ClaveAcceso.of(claveAccesoLote);
    if (isBlank(rucEmisor)) {
      throw new IllegalArgumentException("RUC de emisor requerido para lote");
    }
    if (!isNumeric(rucEmisor) || rucEmisor.length() != 13) {
      throw new IllegalArgumentException("RUC de emisor invalido para lote");
    }
    if (xmlsFirmados == null || xmlsFirmados.isEmpty()) {
      throw new IllegalArgumentException("Lote sin comprobantes");
    }
    if (xmlsFirmados.size() > MAX_COMPROBANTES) {
      throw new IllegalArgumentException("Lote supera maximo de " + MAX_COMPROBANTES + " comprobantes");
    }

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    xml.append("<lote version=\"1.0.0\">");
    xml.append("<claveAcceso>").append(claveAccesoLote).append("</claveAcceso>");
    xml.append("<ruc>").append(rucEmisor).append("</ruc>");
    xml.append("<comprobantes>");
    for (String comprobante : xmlsFirmados) {
      if (isBlank(comprobante)) {
        throw new IllegalArgumentException("Comprobante vacio en lote");
      }
      xml.append("<comprobante><![CDATA[").append(comprobante).append("]]></comprobante>");
    }
    xml.append("</comprobantes>");
    xml.append("</lote>");

    int size = xml.toString().getBytes(StandardCharsets.UTF_8).length;
    if (size > MAX_BYTES) {
      throw new IllegalArgumentException("Lote supera maximo de 512kb");
    }
    return xml.toString();
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private boolean isNumeric(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (!Character.isDigit(value.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}
