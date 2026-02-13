package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.DocumentoProveedorResult;

public interface CrearDocumentoProveedorUseCase {
  DocumentoProveedorResult crear(CrearDocumentoProveedorCommand command);
}
