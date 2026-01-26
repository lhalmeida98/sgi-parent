package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.EmpresaCreateResult;

public interface CrearEmpresaUseCase {
  EmpresaCreateResult crear(CrearEmpresaCommand command);
}
