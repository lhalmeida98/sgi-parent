package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.FacturaCreateResult;

public interface CrearFacturaUseCase {
  FacturaCreateResult crear(CrearFacturaCommand command);
}
