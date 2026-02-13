package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.InventarioProductoDisponibleResult;
import java.util.List;

public interface ListarProductosDisponiblesPorBodegaUseCase {
  List<InventarioProductoDisponibleResult> listar(Long empresaId, Long bodegaId);
}
