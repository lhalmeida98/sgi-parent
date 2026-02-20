package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.RolResult;

public interface ActualizarRolUseCase {
  RolResult actualizar(Long rolId, ActualizarRolCommand command);
}
