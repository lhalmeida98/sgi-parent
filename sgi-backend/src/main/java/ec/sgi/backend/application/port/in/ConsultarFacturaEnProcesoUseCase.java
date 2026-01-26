package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.FacturaProcesoResult;

public interface ConsultarFacturaEnProcesoUseCase {
  FacturaProcesoResult consultarEnProceso(Long facturaId);
}
