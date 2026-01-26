package ec.sri.einvoice.application.port.out;

import ec.sri.einvoice.domain.model.Comprobante;

public interface XmlComprobanteGenerator {
  String generar(Comprobante comprobante);
}
