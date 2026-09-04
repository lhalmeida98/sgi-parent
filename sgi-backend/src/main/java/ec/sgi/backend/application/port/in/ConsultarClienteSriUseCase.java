package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ClienteSriConsultaResult;

public interface ConsultarClienteSriUseCase {
  ClienteSriConsultaResult consultar(String identificacion);
}
