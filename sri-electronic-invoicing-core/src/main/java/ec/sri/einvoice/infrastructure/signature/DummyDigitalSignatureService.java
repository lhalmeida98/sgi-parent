package ec.sri.einvoice.infrastructure.signature;

import ec.sri.einvoice.application.port.out.DigitalSignatureService;
import ec.sri.einvoice.domain.model.InfoTributaria;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.signature", name = "type", havingValue = "dummy", matchIfMissing = true)
public class DummyDigitalSignatureService implements DigitalSignatureService {
  @Override
  public String firmar(String xml, InfoTributaria infoTributaria) {
    return "<Signed>" + xml + "</Signed>";
  }
}
