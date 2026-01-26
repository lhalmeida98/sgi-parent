package ec.sri.einvoice.application.port.in;

import ec.sri.einvoice.domain.model.Detalle;
import ec.sri.einvoice.domain.model.InfoDocumento;
import ec.sri.einvoice.domain.model.InfoTributaria;
import ec.sri.einvoice.domain.model.TipoComprobante;
import java.util.List;

public record EmitirComprobanteCommand(
    TipoComprobante tipo,
    InfoTributaria infoTributaria,
    InfoDocumento infoDocumento,
    List<Detalle> detalles,
    String codigoNumerico
) {
}
