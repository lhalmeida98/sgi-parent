package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ClienteResult;

public interface ActualizarClienteUseCase {
  ClienteResult actualizar(Long empresaId, Long clienteId, ActualizarClienteCommand command);
}
