package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ProductoCreateResult;

public interface CrearProductoUseCase {
  ProductoCreateResult crear(CrearProductoCommand command);
}
