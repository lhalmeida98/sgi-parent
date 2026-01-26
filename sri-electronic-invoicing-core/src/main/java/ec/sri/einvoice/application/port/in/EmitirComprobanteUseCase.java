package ec.sri.einvoice.application.port.in;

import ec.sri.einvoice.domain.model.ComprobanteId;

public interface EmitirComprobanteUseCase {
  ComprobanteId emitir(EmitirComprobanteCommand command);
}
