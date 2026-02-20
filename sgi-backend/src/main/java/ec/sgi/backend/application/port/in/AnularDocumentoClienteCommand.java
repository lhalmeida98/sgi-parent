package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record AnularDocumentoClienteCommand(
    Long empresaId,
    Long documentoClienteId,
    String motivo
) {
  public AnularDocumentoClienteCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(documentoClienteId, "documentoClienteId");
  }
}
