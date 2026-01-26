package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.InventarioResult;
import java.util.List;

public interface ListarInventarioUseCase {
  List<InventarioResult> listar(Long empresaId);
}
