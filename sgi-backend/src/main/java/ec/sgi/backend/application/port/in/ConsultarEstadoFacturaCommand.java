package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record ConsultarEstadoFacturaCommand(Long facturaId) {
  public ConsultarEstadoFacturaCommand {
    Objects.requireNonNull(facturaId, "facturaId");
  }
}
