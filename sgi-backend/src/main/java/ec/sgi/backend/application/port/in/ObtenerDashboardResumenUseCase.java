package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.DashboardResumenResult;

public interface ObtenerDashboardResumenUseCase {
  DashboardResumenResult resumen(Long empresaId);
}
