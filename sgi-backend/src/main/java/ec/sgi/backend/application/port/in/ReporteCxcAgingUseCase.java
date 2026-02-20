package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.CxcAgingResumenResult;

public interface ReporteCxcAgingUseCase {
  CxcAgingResumenResult resumen(Long empresaId, Long clienteId);
}
