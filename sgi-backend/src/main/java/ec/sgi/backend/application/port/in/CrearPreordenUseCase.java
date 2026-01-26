package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.PreordenCreateResult;

public interface CrearPreordenUseCase {
  PreordenCreateResult crear(CrearPreordenCommand command);
}
