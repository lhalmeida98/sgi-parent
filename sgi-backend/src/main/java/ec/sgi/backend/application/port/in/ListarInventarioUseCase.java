package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.InventarioResumenResult;
import java.util.List;

public interface ListarInventarioUseCase {
  List<InventarioResumenResult> listar(Long empresaId);
}
