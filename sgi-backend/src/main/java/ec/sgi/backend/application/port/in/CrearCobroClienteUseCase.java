package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.CobroClienteResult;

public interface CrearCobroClienteUseCase {
  CobroClienteResult crear(CrearCobroClienteCommand command);
}
