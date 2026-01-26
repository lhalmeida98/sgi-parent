package ec.sri.einvoice.application.port.in;

import ec.sri.einvoice.application.port.out.SriConsultaFacturaResponse;

public interface ConsultarFacturaComercialUseCase {
  SriConsultaFacturaResponse consultar(ConsultarFacturaComercialCommand command);
}
