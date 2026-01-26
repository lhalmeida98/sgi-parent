package ec.sri.einvoice.domain.model;

import java.util.UUID;

public record ComprobanteId(UUID value) {
  public static ComprobanteId newId() {
    return new ComprobanteId(UUID.randomUUID());
  }
}
