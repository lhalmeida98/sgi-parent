package ec.sri.einvoice.application.port.out;

import ec.sri.einvoice.domain.model.Ambiente;

public interface SriConsultaClient {
  SriConsultaAutorizacionResponse consultarEstadoAutorizacion(Ambiente ambiente, String claveAcceso);

  SriConsultaFacturaResponse consultarEstadoFacturaComercial(Ambiente ambiente, String claveAcceso);
}
