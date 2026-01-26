package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.FacturaProcesoResult;
import java.util.List;

public interface ListarFacturasEnProcesoUseCase {
  List<FacturaProcesoResult> listarEnProceso(Long empresaId);
}
