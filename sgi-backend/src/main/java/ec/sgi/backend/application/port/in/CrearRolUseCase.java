package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.RolCreateResult;

public interface CrearRolUseCase {
  RolCreateResult crear(CrearRolCommand command);
}
