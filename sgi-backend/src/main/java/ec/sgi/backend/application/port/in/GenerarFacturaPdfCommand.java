package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record GenerarFacturaPdfCommand(Long facturaId, Long empresaId) {
  public GenerarFacturaPdfCommand {
    Objects.requireNonNull(facturaId, "facturaId");
    Objects.requireNonNull(empresaId, "empresaId");
  }
}
