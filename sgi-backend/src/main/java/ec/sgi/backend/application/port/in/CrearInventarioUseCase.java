package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.InventarioCreateResult;

public interface CrearInventarioUseCase {
  InventarioCreateResult crear(CrearInventarioCommand command);
}
