package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.AccionCreateResult;

public interface CrearAccionUseCase {
  AccionCreateResult crear(CrearAccionCommand command);
}
