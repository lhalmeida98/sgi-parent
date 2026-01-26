package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.EmpresaResult;

public interface ActualizarEmpresaUseCase {
  EmpresaResult actualizar(Long empresaId, ActualizarEmpresaCommand command);
}
