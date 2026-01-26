package ec.sri.einvoice.domain.event;

import ec.sri.einvoice.domain.model.ComprobanteId;
import java.time.Instant;
import java.util.UUID;

public record ComprobanteEvent(
    UUID id,
    ComprobanteId comprobanteId,
    ComprobanteEventType tipo,
    String detalle,
    Instant ocurridoEn
) {
}
