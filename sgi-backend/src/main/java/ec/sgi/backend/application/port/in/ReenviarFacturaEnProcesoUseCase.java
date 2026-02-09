package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.FacturaProcesoResult;

public interface ReenviarFacturaEnProcesoUseCase {
  FacturaProcesoResult reenviarEnProceso(Long facturaId, Long empresaId);
}
