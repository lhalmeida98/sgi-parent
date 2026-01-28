package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.EmpresaResult;

public interface SubirLogoEmpresaUseCase {
  EmpresaResult subir(SubirLogoEmpresaCommand command);
}
