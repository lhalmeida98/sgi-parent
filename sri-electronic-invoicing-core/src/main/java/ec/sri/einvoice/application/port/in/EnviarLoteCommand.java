package ec.sri.einvoice.application.port.in;

import ec.sri.einvoice.domain.model.Ambiente;
import java.util.List;

public record EnviarLoteCommand(
    Ambiente ambiente,
    String claveAccesoLote,
    String rucEmisor,
    List<String> xmlsFirmados
) {
}
