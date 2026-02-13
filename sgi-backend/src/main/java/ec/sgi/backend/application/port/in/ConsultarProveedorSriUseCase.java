package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ProveedorSriConsultaResult;

public interface ConsultarProveedorSriUseCase {
  ProveedorSriConsultaResult consultar(String identificacion);
}
