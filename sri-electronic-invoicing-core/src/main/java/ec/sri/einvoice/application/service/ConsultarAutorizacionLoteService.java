package ec.sri.einvoice.application.service;

import ec.sri.einvoice.application.port.in.ConsultarAutorizacionLoteCommand;
import ec.sri.einvoice.application.port.in.ConsultarAutorizacionLoteUseCase;
import ec.sri.einvoice.application.port.out.SriLoteAutorizacionResponse;
import ec.sri.einvoice.application.port.out.SriLoteClient;

public class ConsultarAutorizacionLoteService implements ConsultarAutorizacionLoteUseCase {
  private final SriLoteClient loteClient;

  public ConsultarAutorizacionLoteService(SriLoteClient loteClient) {
    this.loteClient = loteClient;
  }

  @Override
  public SriLoteAutorizacionResponse consultar(ConsultarAutorizacionLoteCommand command) {
    return loteClient.consultarAutorizacionLote(command.ambiente(), command.claveAccesoLote());
  }
}
