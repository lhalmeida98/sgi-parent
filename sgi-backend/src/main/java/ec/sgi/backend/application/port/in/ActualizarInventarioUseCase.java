package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.InventarioDetalleResult;

public interface ActualizarInventarioUseCase {
  InventarioDetalleResult actualizar(Long empresaId, Long productoId, Long bodegaId, ActualizarInventarioCommand command);
}
