package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ProveedorResult;

public interface ActualizarProveedorUseCase {
  ProveedorResult actualizar(Long empresaId, Long proveedorId, ActualizarProveedorCommand command);
}
