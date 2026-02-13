package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ProveedorCreateResult;

public interface CrearProveedorUseCase {
  ProveedorCreateResult crear(CrearProveedorCommand command);
}
