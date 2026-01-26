package ec.sri.einvoice.application.port.out;

import ec.sri.einvoice.domain.model.Ambiente;
import java.util.List;

public interface SriLoteClient {
  SriResponse enviarLote(Ambiente ambiente, String claveAccesoLote, String rucEmisor, List<String> xmlsFirmados);

  SriLoteAutorizacionResponse consultarAutorizacionLote(Ambiente ambiente, String claveAccesoLote);
}
