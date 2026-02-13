package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.DocumentoProveedorPreviewResult;

public interface CrearDocumentoProveedorAutorizacionUseCase {
  DocumentoProveedorPreviewResult crearDesdeAutorizacion(CrearDocumentoProveedorAutorizacionCommand command);
}
