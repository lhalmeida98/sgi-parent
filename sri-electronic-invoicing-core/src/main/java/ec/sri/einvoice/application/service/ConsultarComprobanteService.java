package ec.sri.einvoice.application.service;

import ec.sri.einvoice.application.port.in.ConsultarComprobanteCommand;
import ec.sri.einvoice.application.port.in.ConsultarComprobanteUseCase;
import ec.sri.einvoice.application.port.out.SriConsultaAutorizacionResponse;
import ec.sri.einvoice.application.port.out.SriConsultaClient;

public class ConsultarComprobanteService implements ConsultarComprobanteUseCase {
  private final SriConsultaClient consultaClient;

  public ConsultarComprobanteService(SriConsultaClient consultaClient) {
    this.consultaClient = consultaClient;
  }

  @Override
  public SriConsultaAutorizacionResponse consultar(ConsultarComprobanteCommand command) {
    return consultaClient.consultarEstadoAutorizacion(command.ambiente(), command.claveAcceso());
  }
}
