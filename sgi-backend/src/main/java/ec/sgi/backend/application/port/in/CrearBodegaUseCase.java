package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.BodegaCreateResult;

public interface CrearBodegaUseCase {
  BodegaCreateResult crear(CrearBodegaCommand command);
}
