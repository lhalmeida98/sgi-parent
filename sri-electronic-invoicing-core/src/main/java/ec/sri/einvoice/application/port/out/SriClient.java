package ec.sri.einvoice.application.port.out;

import ec.sri.einvoice.domain.model.Comprobante;

public interface SriClient {
  SriResponse enviar(Comprobante comprobante, String xmlFirmado);
}
