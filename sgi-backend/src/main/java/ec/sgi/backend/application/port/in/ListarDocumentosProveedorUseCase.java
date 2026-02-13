package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.DocumentoProveedorResult;
import java.util.List;

public interface ListarDocumentosProveedorUseCase {
  List<DocumentoProveedorResult> listar(Long empresaId, Long proveedorId);
}
