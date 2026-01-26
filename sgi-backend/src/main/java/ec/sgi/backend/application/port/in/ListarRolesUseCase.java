package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.RolResult;
import java.util.List;

public interface ListarRolesUseCase {
  List<RolResult> listar(Long empresaId);
}
