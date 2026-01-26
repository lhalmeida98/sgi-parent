package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ClienteCreateResult;

public interface CrearClienteUseCase {
  ClienteCreateResult crear(CrearClienteCommand command);
}
