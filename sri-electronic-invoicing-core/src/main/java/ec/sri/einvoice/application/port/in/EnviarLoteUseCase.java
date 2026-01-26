package ec.sri.einvoice.application.port.in;

import ec.sri.einvoice.application.port.out.SriResponse;

public interface EnviarLoteUseCase {
  SriResponse enviar(EnviarLoteCommand command);
}
