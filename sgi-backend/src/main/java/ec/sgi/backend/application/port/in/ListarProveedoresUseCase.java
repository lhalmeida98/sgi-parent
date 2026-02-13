package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ProveedorResult;
import java.util.List;

public interface ListarProveedoresUseCase {
  List<ProveedorResult> listar(Long empresaId);
}
