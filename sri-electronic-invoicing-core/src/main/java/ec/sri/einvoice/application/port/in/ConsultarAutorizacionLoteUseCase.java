package ec.sri.einvoice.application.port.in;

import ec.sri.einvoice.application.port.out.SriLoteAutorizacionResponse;

public interface ConsultarAutorizacionLoteUseCase {
  SriLoteAutorizacionResponse consultar(ConsultarAutorizacionLoteCommand command);
}
