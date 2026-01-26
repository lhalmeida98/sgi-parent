package ec.sri.einvoice.application.port.out;

import ec.sri.einvoice.domain.model.TipoComprobante;

public interface SriXmlValidator {
  void validar(TipoComprobante tipoComprobante, String xml);
}
