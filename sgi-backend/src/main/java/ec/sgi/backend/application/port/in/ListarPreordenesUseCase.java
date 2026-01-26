package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.PreordenResult;
import java.util.List;

public interface ListarPreordenesUseCase {
  List<PreordenResult> listar(Long empresaId);
}
