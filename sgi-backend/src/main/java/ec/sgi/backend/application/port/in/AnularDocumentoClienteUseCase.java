package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.DocumentoClienteResult;

public interface AnularDocumentoClienteUseCase {
  DocumentoClienteResult anular(AnularDocumentoClienteCommand command);
}
