package ec.sri.einvoice.application.service;

import ec.sri.einvoice.application.port.in.ConsultarFacturaComercialCommand;
import ec.sri.einvoice.application.port.in.ConsultarFacturaComercialUseCase;
import ec.sri.einvoice.application.port.out.SriConsultaClient;
import ec.sri.einvoice.application.port.out.SriConsultaFacturaResponse;

public class ConsultarFacturaComercialService implements ConsultarFacturaComercialUseCase {
  private final SriConsultaClient consultaClient;

  public ConsultarFacturaComercialService(SriConsultaClient consultaClient) {
    this.consultaClient = consultaClient;
  }

  @Override
  public SriConsultaFacturaResponse consultar(ConsultarFacturaComercialCommand command) {
    return consultaClient.consultarEstadoFacturaComercial(command.ambiente(), command.claveAcceso());
  }
}
