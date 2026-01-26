package ec.sri.einvoice.application.port.in;

import ec.sri.einvoice.domain.model.Ambiente;

public record ConsultarAutorizacionLoteCommand(
    Ambiente ambiente,
    String claveAccesoLote
) {
}
