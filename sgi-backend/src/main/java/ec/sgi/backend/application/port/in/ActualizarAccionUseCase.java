package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.AccionResult;

public interface ActualizarAccionUseCase {
  AccionResult actualizar(Long accionId, ActualizarAccionCommand command);
}
