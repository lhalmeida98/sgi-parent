package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ProductoResult;

public interface ActualizarProductoUseCase {
  ProductoResult actualizar(Long empresaId, Long productoId, ActualizarProductoCommand command);
}
