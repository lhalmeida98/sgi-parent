package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record ConsultarEstadoFacturaCommand(Long empresaId, String numeroFactura) {
  public ConsultarEstadoFacturaCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(numeroFactura, "numeroFactura");
  }
}
