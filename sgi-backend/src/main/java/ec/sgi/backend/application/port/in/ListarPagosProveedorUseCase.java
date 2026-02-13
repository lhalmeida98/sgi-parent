package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.PagoProveedorResult;
import java.util.List;

public interface ListarPagosProveedorUseCase {
  List<PagoProveedorResult> listar(Long empresaId, Long proveedorId);
}
