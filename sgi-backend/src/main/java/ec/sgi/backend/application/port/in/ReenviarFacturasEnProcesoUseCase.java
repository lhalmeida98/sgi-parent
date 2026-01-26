package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.FacturaProcesoResult;
import java.util.List;

public interface ReenviarFacturasEnProcesoUseCase {
  List<FacturaProcesoResult> reenviarEnProceso(Long empresaId);
}
