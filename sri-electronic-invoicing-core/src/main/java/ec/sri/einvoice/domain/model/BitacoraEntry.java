package ec.sri.einvoice.domain.model;

import java.time.Instant;
import java.util.UUID;

public record BitacoraEntry(
    UUID id,
    ComprobanteId comprobanteId,
    String accion,
    String detalle,
    Instant ocurridoEn
) {
}
