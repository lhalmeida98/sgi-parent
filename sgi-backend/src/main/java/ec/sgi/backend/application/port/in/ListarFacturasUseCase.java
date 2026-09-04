package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.FacturaResumenPageResult;
import java.time.LocalDate;

public interface ListarFacturasUseCase {
  FacturaResumenPageResult listarPorEmpresa(Long empresaId, LocalDate fechaDesde, LocalDate fechaHasta, int page,
      int size, String ambiente);
}
