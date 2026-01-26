package ec.sri.einvoice.application.service;

import ec.sri.einvoice.application.port.in.EnviarLoteCommand;
import ec.sri.einvoice.application.port.in.EnviarLoteUseCase;
import ec.sri.einvoice.application.port.out.SriLoteClient;
import ec.sri.einvoice.application.port.out.SriResponse;

public class EnviarLoteService implements EnviarLoteUseCase {
  private final SriLoteClient loteClient;

  public EnviarLoteService(SriLoteClient loteClient) {
    this.loteClient = loteClient;
  }

  @Override
  public SriResponse enviar(EnviarLoteCommand command) {
    return loteClient.enviarLote(
        command.ambiente(),
        command.claveAccesoLote(),
        command.rucEmisor(),
        command.xmlsFirmados()
    );
  }
}
