package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.CobroClienteResult;
import java.util.List;

public interface ListarCobrosClienteUseCase {
  List<CobroClienteResult> listar(Long empresaId, Long clienteId);
}
