package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.FacturaEstadoResult;

public interface ConsultarEstadoFacturaUseCase {
  FacturaEstadoResult consultar(ConsultarEstadoFacturaCommand command);
}
