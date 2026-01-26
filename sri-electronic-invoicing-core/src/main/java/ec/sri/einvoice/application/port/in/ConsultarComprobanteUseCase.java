package ec.sri.einvoice.application.port.in;

import ec.sri.einvoice.application.port.out.SriConsultaAutorizacionResponse;

public interface ConsultarComprobanteUseCase {
  SriConsultaAutorizacionResponse consultar(ConsultarComprobanteCommand command);
}
