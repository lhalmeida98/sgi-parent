package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ImpuestoResult;
import java.util.List;

public interface ListarImpuestosUseCase {
  List<ImpuestoResult> listar(Long empresaId);
}
