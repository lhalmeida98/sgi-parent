package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.CuentaPorCobrarResult;
import java.util.List;

public interface ListarCuentasPorCobrarUseCase {
  List<CuentaPorCobrarResult> listar(Long empresaId, Long clienteId);
}
