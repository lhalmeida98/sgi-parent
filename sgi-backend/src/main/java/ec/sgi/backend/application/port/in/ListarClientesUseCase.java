package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ClienteResult;
import java.util.List;

public interface ListarClientesUseCase {
  List<ClienteResult> listar(Long empresaId);
}
