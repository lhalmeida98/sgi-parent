package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.DocumentoClienteResult;
import java.util.List;

public interface ListarDocumentosClienteUseCase {
  List<DocumentoClienteResult> listar(Long empresaId, Long clienteId);
}
