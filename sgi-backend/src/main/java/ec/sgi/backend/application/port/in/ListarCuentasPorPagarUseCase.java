package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.CuentaPorPagarResult;
import java.util.List;

public interface ListarCuentasPorPagarUseCase {
  List<CuentaPorPagarResult> listar(Long empresaId, Long proveedorId);
}
