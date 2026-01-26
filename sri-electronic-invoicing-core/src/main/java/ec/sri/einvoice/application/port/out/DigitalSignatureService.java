package ec.sri.einvoice.application.port.out;

import ec.sri.einvoice.domain.model.InfoTributaria;

public interface DigitalSignatureService {
  String firmar(String xml, InfoTributaria infoTributaria);
}
