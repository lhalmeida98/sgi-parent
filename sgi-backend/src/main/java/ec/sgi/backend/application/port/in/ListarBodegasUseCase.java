package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.BodegaResult;
import java.util.List;

public interface ListarBodegasUseCase {
  List<BodegaResult> listar(Long empresaId);
}
